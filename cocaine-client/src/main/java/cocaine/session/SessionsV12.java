package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.messagev12.MessageV12;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

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

    public SessionsV12(String service) {
        this.service = service;
        this.counter = new AtomicLong(1);
        this.sessions = new ConcurrentHashMap<>();
    }

    public <T> SessionV12<T> create(TransactionTree rx, TransactionTree tx) {
        long id = counter.getAndIncrement();

        logger.debug("Creating new session: " + id);
        SessionV12 session = new SessionV12(id, service, rx, tx, new PrimitiveProtocol());
        sessions.put(id, session);
        return session;
    }

    public void onEvent(MessageV12 msg) {
        SessionV12 session = sessions.get(msg.getSession());
        if (session != null) {
            session.rx().onRead(msg.getType(), msg.getPayload());
        } else {
            logger.debug("Unknown mesage for service: " + service + ", message typ: " +
                    msg.getType() + ", session:" + msg.getSession());
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
