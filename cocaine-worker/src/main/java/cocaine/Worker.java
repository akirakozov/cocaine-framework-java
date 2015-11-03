package cocaine;

import java.io.IOException;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import cocaine.message.*;
import cocaine.messagev12.MessageV12;
import cocaine.msgpack.MessageV12Template;
import com.etsy.net.JUDS;
import com.etsy.net.UnixDomainSocket;
import com.etsy.net.UnixDomainSocketClient;
import com.google.common.base.Throwables;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.unpacker.Unpacker;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public class Worker implements AutoCloseable {

    private static final Logger logger = Logger.getLogger(Worker.class);

    private static final Message HEARTBEAT = Messages.heartbeat();

    private final MessagePack pack;
    private final WorkerOptions options;
    private final Map<String, EventHandler> handlers;
    private final WorkerSessions sessions;
    private final long maxSession;
    private final Timer heartbeats;
    private final Timer disowns;
    private final Dispatcher dispatcher;

    private UnixDomainSocket socket;
    private Disown disown;

    Worker(WorkerOptions options, Map<String, EventHandler> handlers) {
        this.pack = new MessagePack();
        this.pack.register(MessageV12.class, MessageV12Template.getInstance());
        this.options = options;
        this.handlers = handlers;
        this.sessions = new WorkerSessions(this);
        this.maxSession = 0L;
        this.heartbeats = new Timer(getThreadName("Worker Heartbeats"), true);
        this.disowns = new Timer(getThreadName("Worker Disown"), true);
        this.dispatcher = new Dispatcher();
    }

    public void run() throws IOException {
        this.socket = new UnixDomainSocketClient(options.getEndpoint(), JUDS.SOCK_STREAM);
        this.sendHandShake();
        this.heartbeats.scheduleAtFixedRate(new Heartbeat(), 0, options.getHeartbeatTimeout());
        this.dispatcher.start();
    }

    public void stop() {
        this.dispatcher.interrupt();
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

    private void dispatch(MessageV12 msg) {
        if (msg.getSession() == 1) {
            if (msg.getType() == MessageType.HEARTBEAT.value()) {
                dispatchHeartbeat(new HeartbeatMessage());
            } else if (msg.getType() == MessageType.TERMINATE.value()) {
                // TODO: read reason and message from payload
                dispatchTerminate(new TerminateMessage(TerminateMessage.Reason.NORMAL, ""));
            } else {
                logger.warn("Unexpected message type " + msg.getType() + " on session" + msg.getSession());
            }
            return;
        }

        if (maxSession < msg.getSession()) {
            if (msg.getType() == MessageType.INVOKE.value()) {
                String event = msg.getPayload().asRawValue().getString();
                dispatchInvoke(new InvokeMessage(msg.getSession(), event));
            } else {
                logger.warn("New session must " + msg.getSession()
                        + " start from invoke instead of " + msg.getType());
            }
            return;
        }

        if (msg.getType() == MessageType.WRITE.value()) {
            byte[] data = msg.getPayload().asRawValue().getByteArray();
            dispatchWrite(new WriteMessage(msg.getSession(), data));
        } else if (msg.getType() == MessageType.CLOSE.value()) {
            dispatchClose(new CloseMessage(msg.getSession()));
        } else if (msg.getType() == MessageType.ERROR.value()) {
            // TODO: read error code and message from payload
            dispatchError(new ErrorMessage(msg.getSession(), 0, ""));
        } else {
            logger.warn("Unexpected message type " + msg.getType() + " on session" + msg.getSession());
        }
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

        try {
            String event = msg.getEvent();
            if (this.handlers.containsKey(event)) {
                this.handlers.get(event).handle(session.getInput(), session.getOutput());
            } else {
                logger.warn("No handler for '" + event + "' event is registered");
                write(Messages.error(msg.getSession(), ErrorMessage.Code.ENOHANDLER,
                        "No handler for '" + event + "' event is registered"));
            }
        } catch (Exception e) {
            logger.error("Invocation failed: " + e.getMessage());
            write(Messages.error(msg.getSession(), ErrorMessage.Code.EINVFAILED,
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
                new ClientErrorException(options.getApplication(), msg.getMessage(), msg.getCode()));
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
        this.write(Messages.choke(session));
    }

    void sendChunk(long session, byte[] data) {
        this.write(Messages.chunk(session, data));
    }

    void sendError(long session, int code, String message) {
        this.write(Messages.error(session, code, message));
    }

    private void write(Message message) {
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
                Unpacker unpacker = pack.createUnpacker(Worker.this.socket.getInputStream());
                while (true) {
                    Worker.this.dispatch(unpacker.read(MessageV12.class));
                }
            } catch (Exception e) {
                if (!isInterrupted()) {
                    throw Throwables.propagate(e);
                }
                logger.info(getName() + " has been interrupted");
            }
        }

    }

}
