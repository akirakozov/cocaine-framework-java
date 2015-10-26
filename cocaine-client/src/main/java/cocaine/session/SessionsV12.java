package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.messagev12.MessageV12;
import cocaine.session.protocol.CocaineProtocol;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author akirakozov
 */
public class SessionsV12 {
    private static final Logger logger = Logger.getLogger(SessionsV12.class);

    private final AtomicLong counter;
    private final Map<Long, SessionV12> sessions;
    private final String service;
    private final CocaineProtocolsRegistry protocolsRegistry;

    public SessionsV12(String service, CocaineProtocolsRegistry protocolsRegistry) {
        this.service = service;
        this.counter = new AtomicLong(1);
        this.sessions = new ConcurrentHashMap<>();
        this.protocolsRegistry = protocolsRegistry;
    }

    public <T> SessionV12<T> create(
            TransactionTree rx, TransactionTree tx,
            CocainePayloadDeserializer<T> deserializer)
    {
        long id = counter.getAndIncrement();

        logger.debug("Creating new session: " + id);
        CocaineProtocol protocol = protocolsRegistry.findProtocol(rx);
        SessionV12 session = new SessionV12(id, service, rx, tx, protocol, deserializer);
        sessions.put(id, session);
        return session;
    }

    public SessionV12<Value> create(TransactionTree rx, TransactionTree tx) {
        return create(rx, tx, new ValueIdentityPayloadDeserializer());
    }

    public void onEvent(MessageV12 msg) {
        SessionV12 session = sessions.get(msg.getSession());
        if (session != null) {
            session.rx().onRead(msg.getType(), msg.getPayload());
        } else {
            logger.debug("Unknown mesage for service " + service
                    + ", session:" + msg.getSession() + ", type: " + msg.getType());
        }
    }

    public void onCompleted(long id) {
        SessionV12 session = sessions.remove(id);
        if (session != null) {
            logger.debug("Closing session " + id);
            session.rx().onCompleted();
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
}
