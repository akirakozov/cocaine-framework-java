package cocaine.session;

import cocaine.ServiceException;
import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import cocaine.session.protocol.CocaineProtocol;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class ReceiveChannel<T> {
    private static final Logger logger = Logger.getLogger(ReceiveChannel.class);

    private TransactionTree rxTree;
    private final Subject<ResultMessage, ResultMessage> subject;
    private final AtomicBoolean isDone;
    private final CocaineProtocol protocol;
    private final CocainePayloadDeserializer<T> deserializer;
    private final String serviceName;

    public ReceiveChannel(
            String serviceName,
            TransactionTree rxTree,
            CocaineProtocol protocol,
            CocainePayloadDeserializer<T> deserializer)
    {
        this.serviceName = serviceName;
        this.rxTree = rxTree;
        this.subject = ReplaySubject.create();
        this.isDone = new AtomicBoolean(false);
        this.protocol = protocol;
        this.deserializer = deserializer;
    }

    public T get() {
        ResultMessage msg = subject.toBlocking().first();
        Value payload = protocol.handle(serviceName, msg.messageType, msg.payload);
        try {
            return deserializer.deserialize(msg.messageType, payload);
        } catch (IOException e) {
            logger.error(
                    "Couldn't deserialize result of message " + msg.messageType + ", " + e.getMessage(), e);
            throw new ServiceException(serviceName, e.getMessage());
        }
    }

    void onRead(int type, Value payload) {
        TransactionInfo info = rxTree.getInfo(type);
        if (info == null) {
            // TODO: handle incorrect receive message
        }
        subject.onNext(new ResultMessage(info.getMessageName(), payload));
        if (!info.getTree().isCycle()) {
            if (info.getTree().isEmpty()) {
                done();
            } else {
                rxTree = info.getTree();
            }
        }
        // TODO: add support of cycle

    }

    public void onCompleted() {
        subject.onCompleted();
        done();
    }

    private void done() {
        isDone.set(true);
    }

    private static class ResultMessage {
        private String messageType;
        private Value payload;

        public ResultMessage(String messageType, Value payload) {
            this.messageType = messageType;
            this.payload = payload;
        }
    }
}
