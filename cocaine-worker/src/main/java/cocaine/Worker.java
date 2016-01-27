package cocaine;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import cocaine.message.*;
import cocaine.message.Message;
import cocaine.message.WorkerMessageTemplate;
import cocaine.msgpack.MessageTemplate;
import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocket;
import com.etsy.net.UnixDomainSocketClient;
import com.google.common.base.Throwables;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.type.ArrayValue;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 * @author akirakozov
 */
public class Worker implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(Worker.class);

    private static final WorkerMessage HEARTBEAT = Messages.heartbeat();

    private final MessagePack pack;
    private final WorkerOptions options;
    private final Invoker invoker;
    private final WorkerSessions sessions;
    private final Timer heartbeats;
    private final Timer disowns;
    private final Dispatcher dispatcher;
    private long maxSession;

    private UnixDomainSocket socket;
    private Disown disown;

    public Worker(WorkerOptions options, Map<String, EventHandler> handlers) {
        this(options, DefaultInvoker.createFromHandlers(handlers));
    }

    public Worker(WorkerOptions options, Invoker invoker) {
        this.pack = new MessagePack();
        this.pack.register(Message.class, MessageTemplate.getInstance());
        this.pack.register(WorkerMessage.class, WorkerMessageTemplate.getInstance());
        this.options = options;
        this.invoker = invoker;
        this.sessions = new WorkerSessions(this);
        this.maxSession = 0L;
        this.heartbeats = new Timer(getThreadName("Worker Heartbeats"), true);
        this.disowns = new Timer(getThreadName("Worker Disown"), true);
        this.dispatcher = new Dispatcher();
    }

    public void run() throws IOException {
        logger.info("Start worker with options: " + options);
        this.socket = new UnixDomainSocketClient(options.getEndpoint(), JUDS.SOCK_STREAM);
        this.sendHandShake();
        this.heartbeats.scheduleAtFixedRate(new Heartbeat(), 0, options.getHeartbeatTimeout());
        this.dispatcher.start();
    }

    public void stop() {
        stopDispatcher();
        this.heartbeats.cancel();
        this.disowns.cancel();
        this.sessions.onCompleted();
        if (this.socket != null) {
            this.socket.close();
        }
    }

    public void terminate(TerminateMessage.Reason reason, String message) {
        this.write(Messages.terminate(reason, message));
        this.stop();
    }

    public void join() throws InterruptedException {
        this.dispatcher.join();
    }

    @Override
    public void close() {
        stop();
    }

    protected void stopDispatcher() {
        this.dispatcher.interrupt();
    }

    private void dispatch(Message message) {
        Optional<WorkerMessage> workerMessage = toWorkerMessage(message);

        if (!workerMessage.isPresent()) {
            logger.warn("Unexpected message type " + message.getType() + " on session" + message.getSession());
            return;
        }
        WorkerMessage msg = workerMessage.get();
        logger.info(msg);

        switch (msg.getType()) {
            case HEARTBEAT:
                dispatchHeartbeat((HeartbeatMessage) msg);
                break;
            case TERMINATE:
                dispatchTerminate((TerminateMessage) msg);
                break;
            case INVOKE:
                dispatchInvoke((InvokeMessage) msg);
                break;
            case WRITE:
                dispatchWrite((WriteMessage) msg);
                break;
            case CLOSE:
                dispatchClose((CloseMessage) msg);
                break;
            case ERROR:
                dispatchError((ErrorMessage) msg);
                break;
            default:
                logger.warn("Unexpected message type: " + msg.getType());
        }
    }

    private Optional<WorkerMessage> toWorkerMessage(Message msg) {
        if (msg.getSession() == 1) {
            if (msg.getType() == MessageType.HEARTBEAT.value()) {
                return Optional.of(Messages.heartbeat());
            } else if (msg.getType() == MessageType.TERMINATE.value()) {
                // TODO: read reason and message from payload
                return Optional.of(Messages.terminate(TerminateMessage.Reason.NORMAL, ""));
            }
            return Optional.empty();
        }

        if (maxSession < msg.getSession()) {
            maxSession = msg.getSession();
            if (msg.getType() == MessageType.INVOKE.value()) {
                String event = msg.getPayload().asArrayValue().get(0).asRawValue().getString();
                return Optional.of(Messages.invoke(msg.getSession(), event));
            }
            return Optional.empty();
        }

        if (msg.getType() == MessageType.WRITE.value()) {
            byte[] data = msg.getPayload().asArrayValue().get(0).asRawValue().getByteArray();
            return Optional.of(Messages.write(msg.getSession(), data));
        } else if (msg.getType() == MessageType.CLOSE.value()) {
            return Optional.of(Messages.close(msg.getSession()));
        } else if (msg.getType() == MessageType.ERROR.value()) {
            ArrayValue values = msg.getPayload().asArrayValue();
            ArrayValue errCodes = values.get(0).asArrayValue();
            int category = errCodes.get(0).asIntegerValue().getInt();
            int error = errCodes.get(1).asIntegerValue().getInt();
            String errMsg = values.size() == 2 ? values.get(1).asRawValue().getString() : "";
            return Optional.of(Messages.error(msg.getSession(), category, error, errMsg));
        }

        return Optional.empty();
    }

    private void dispatchHeartbeat(HeartbeatMessage msg) {
        logger.debug("Heartbeat has been received. Stop disown timer");
        if (this.disown != null) {
            this.disown.cancel();
        }
    }

    private void dispatchTerminate(TerminateMessage msg) {
        logger.debug("Terminate has been received " + msg.getReason() + " " + msg.getMessage());
        this.terminate(msg.getReason(), msg.getMessage());
    }

    private void dispatchInvoke(InvokeMessage msg) {
        logger.debug("Invoke has been received " + msg);
        WorkerSessions.Session session = this.sessions.create(msg.getSession());

        String event = msg.getEvent();
        try {
            this.invoker.invoke(event, session.getInput(), session.getOutput());
        } catch (UnknownClientMethodException e) {
            logger.warn("No handler for '" + event + "' event is registered");
            write(Messages.error(msg.getSession(), ErrorMessage.Category.FRAMEWORK, ErrorMessage.Code.ENOHANDLER,
                    "No handler for '" + event + "' event is registered"));
        } catch (Exception e) {
            logger.error("Invocation failed: " + e.getMessage());
            write(Messages.error(msg.getSession(), ErrorMessage.Category.FRAMEWORK, ErrorMessage.Code.EINVFAILED,
                    "Invocation failed: " + e.getMessage()));
        }
    }

    private void dispatchWrite(WriteMessage msg) {
        logger.debug("Chunk has been received " + msg.getSession());
        try {
            this.sessions.onChunk(msg.getSession(), msg.getData());
        } catch (Exception e) {
            logger.error("Push has failed: " + e.getMessage(), e);
        }
    }

    private void dispatchClose(CloseMessage msg) {
        logger.debug("Choke has been received " + msg.getSession());
        this.sessions.onCompleted(msg.getSession());
    }

    private void dispatchError(ErrorMessage msg) {
        logger.debug("Error has been received " + msg.getCode() + " " + msg.getMessage());
        this.sessions.onError(msg.getSession(),
                new ClientErrorException(msg.getMessage(), msg.getCode()));
    }

    void sendHandShake() {
        this.write(Messages.handshake(options.getUuid()));
    }

    void sendHeartbeat() {
        this.disown = new Disown();
        this.disowns.schedule(this.disown, options.getDisownTimeout());
        this.write(HEARTBEAT);
        logger.debug("Heartbeat has been sent. Start disown timer");
    }

    void sendChoke(long session) {
        this.write(Messages.close(session));
    }

    void sendChunk(long session, byte[] data) {
        this.write(Messages.write(session, data));
    }

    void sendError(long session, int category, int code, String message) {
        this.write(Messages.error(session, category, code, message));
    }

    private void write(WorkerMessage message) {
        if (socket == null) {
            logger.error("No socket connection was established");
            return;
        }
        try {
            socket.getOutputStream().write(pack.write(message));
            socket.getOutputStream().flush();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String getThreadName(String name) {
        return String.format("[%s] [%s] %s", options.getName(), options.getUuid(), name);
    }

    protected InputStream getSocketInputStream() {
        return socket.getInputStream();
    }

    private final class Heartbeat extends TimerTask {

        @Override
        public void run() {
            Worker.this.sendHeartbeat();
        }

    }

    private final class Disown extends TimerTask {

        @Override
        public void run() {
            logger.error("Disowned");
            Worker.this.stop();
        }

    }

    private final class Dispatcher extends Thread {

        public Dispatcher() {
            super(Worker.this.getThreadName("Worker Dispatcher"));
        }

        @Override
        public void run() {
            try {
                Unpacker unpacker = pack.createUnpacker(getSocketInputStream());
                while (true) {
                    try {
                        Worker.this.dispatch(unpacker.read(Message.class));
                    } catch (Exception e) {
                        logger.warn(e, e);
                        Thread.sleep(1000);
                    }
                }
            } catch (Exception e) {
                logger.warn(e, e);
                if (!isInterrupted()) {
                    throw Throwables.propagate(e);
                }
                logger.info(getName() + " has been interrupted");
            }
        }

    }

}
