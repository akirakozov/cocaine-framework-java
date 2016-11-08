package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.message.Message;
import cocaine.session.protocol.CocaineProtocol;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;

/**
 * @author akirakozov
 */
public class Sessions {
    private static final Logger logger = Logger.getLogger(Sessions.class);

    private final AtomicLong counter;
    private final Map<Long, Session> sessions;
    private final String service;
    private final long readTimeoutInMs;
    private final CocaineProtocolsRegistry protocolsRegistry;

    public Sessions(String service, long readTimeoutInMs, CocaineProtocolsRegistry protocolsRegistry) {
        this.service = service;
        this.readTimeoutInMs = readTimeoutInMs;
        this.counter = new AtomicLong(1);
        this.sessions = new ConcurrentHashMap<>();
        this.protocolsRegistry = protocolsRegistry;
    }

    public String getService() {
        return service;
    }

    public <T> Session<T> create(TransactionTree rx, TransactionTree tx,
            Channel channel, Function<Channel, Future<Void>> closeChannelCallback,
            CocainePayloadDeserializer<T> deserializer)
    {
        long id = counter.getAndIncrement();

        logger.debug("Creating new session: " + id);
        CocaineProtocol protocol = protocolsRegistry.findProtocol(rx);
        Session session = new Session(id, service, rx, tx, readTimeoutInMs, protocol, this,
                channel, closeChannelCallbackToRunnable(closeChannelCallback, channel), deserializer);
        sessions.put(id, session);
        return session;
    }

    public Session<Value> create(TransactionTree rx, TransactionTree tx,
            Channel channel, Function<Channel, Future<Void>> closeChannelCallback)
    {
        return create(rx, tx, channel, closeChannelCallback, new ValueIdentityPayloadDeserializer());
    }

    public void onEvent(Message msg) {
        Session session = sessions.get(msg.getSession());
        if (session != null) {
            session.rx().onRead(msg.getType(), msg.getPayload());
        } else {
            logger.debug("Unknown message for service " + service
                    + ", session:" + msg.getSession() + ", type: " + msg.getType());
        }
    }

    public void onCompleted(long id) {
        Session session = sessions.remove(id);
        if (session != null) {
            logger.debug("Closing session " + id);
            session.onCompleted();
        } else {
            logger.warn("Session " + id + " does not exist");
        }
    }

    public void onCompleted() {
        logger.debug("Closing all sessions of " + service);
        for (long session : sessions.keySet()) {
            onCompleted(session);
        }
    }

    public void removeSession(long id) {
        sessions.remove(id);
    }

    private Runnable closeChannelCallbackToRunnable(Function<Channel, Future<Void>> closeChannelCallback,
            Channel channel)
    {
        return () -> {
            try {
                closeChannelCallback.apply(channel).get();
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        };
    }
}
