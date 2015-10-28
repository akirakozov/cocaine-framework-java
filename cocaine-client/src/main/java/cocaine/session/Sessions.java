package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.messagev12.MessageV12;
import cocaine.session.protocol.CocaineProtocol;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import io.netty.channel.Channel;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author akirakozov
 */
public class Sessions {
    private static final Logger logger = Logger.getLogger(Sessions.class);

    private final AtomicLong counter;
    private final Map<Long, Session> sessions;
    private final String service;
    private final CocaineProtocolsRegistry protocolsRegistry;

    public Sessions(String service, CocaineProtocolsRegistry protocolsRegistry) {
        this.service = service;
        this.counter = new AtomicLong(1);
        this.sessions = new ConcurrentHashMap<>();
        this.protocolsRegistry = protocolsRegistry;
    }

    public <T> Session<T> create(
            TransactionTree rx, TransactionTree tx,
            Channel channel, CocainePayloadDeserializer<T> deserializer)
    {
        long id = counter.getAndIncrement();

        logger.debug("Creating new session: " + id);
        CocaineProtocol protocol = protocolsRegistry.findProtocol(rx);
        Session session = new Session(id, service, rx, tx, protocol, this, channel, deserializer);
        sessions.put(id, session);
        return session;
    }

    public Session<Value> create(TransactionTree rx, TransactionTree tx, Channel channel) {
        return create(rx, tx, channel, new ValueIdentityPayloadDeserializer());
    }

    public void onEvent(MessageV12 msg) {
        Session session = sessions.get(msg.getSession());
        if (session != null) {
            session.rx().onRead(msg.getType(), msg.getPayload());
        } else {
            logger.debug("Unknown mesage for service " + service
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
}
