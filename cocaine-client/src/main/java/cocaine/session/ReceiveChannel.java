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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author akirakozov
 */
public class ReceiveChannel<T> {
    private static final Logger logger = Logger.getLogger(ReceiveChannel.class);

    private TransactionTree rxTree;
    private final Subject<ResultMessage, ResultMessage> subject;
    private final AtomicBoolean isDone;
    private final AtomicInteger curMessageNum;
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
        this.curMessageNum = new AtomicInteger(0);
        this.protocol = protocol;
        this.deserializer = deserializer;
    }

    public T get() {
        checkIsDone();

        ResultMessage msg = subject.skip(curMessageNum.getAndIncrement()).toBlocking().first();
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
        TransactionInfo info = rxTree.getInfo(type).get();
        if (info == null) {
            isDone.set(true);
            subject.onError(new ServiceException(serviceName, "Unknown message type: " + type));
            logger.error("Unknown message type: " + type + ", for service " + serviceName);
        }
        subject.onNext(new ResultMessage(info.getMessageName(), payload));
        if (!info.getTree().isCycle()) {
            if (info.getTree().isEmpty()) {
                onCompleted();
                logger.info("Last message received");
            } else {
                rxTree = info.getTree();
            }
        }
    }

    public void onCompleted() {
        done();
        subject.onCompleted();
    }

    public void done() {
        isDone.set(true);
    }

    private void checkIsDone() {
        if (isDone.get()) {
            throw new ServiceException(serviceName, "Session is completed.");
        }
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
