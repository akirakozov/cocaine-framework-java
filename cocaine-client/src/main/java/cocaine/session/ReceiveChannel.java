package cocaine.session;

import cocaine.ServiceException;
import cocaine.UnexpectedServiceMessageException;
import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import cocaine.session.protocol.CocaineProtocol;
import cocaine.session.protocol.IdentityProtocol;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author akirakozov
 */
public class ReceiveChannel<T> {
    private static final Logger logger = Logger.getLogger(ReceiveChannel.class);

    private TransactionTree rxTree;
    private final Subject<ResultMessage, ResultMessage> subject;
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
        this.curMessageNum = new AtomicInteger(0);
        this.protocol = protocol;
        this.deserializer = deserializer;
    }

    public T get() {
        if (protocol instanceof IdentityProtocol) {
            subject.onCompleted();
            return null;
        }

        ResultMessage msg = subject.skip(curMessageNum.getAndIncrement()).toBlocking().first();
        Value payload = protocol.handle(serviceName, msg.messageType, msg.payload);
        try {
            if (payload != null) {
                return deserializer.deserialize(msg.messageType, payload);
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error(
                    "Couldn't deserialize result of message " + msg.messageType + ", " + e.getMessage(), e);
            throw new ServiceException(serviceName, e.getMessage());
        }
    }

    void onRead(int type, Value payload) {
        Optional<TransactionInfo> info = rxTree.getInfo(type);
        if (!info.isPresent()) {
            subject.onError(new UnexpectedServiceMessageException(serviceName, type));
            logger.error("Unknown message type: " + type + ", for service " + serviceName);
        } else {
            subject.onNext(new ResultMessage(info.get().getMessageName(), payload));
            TransactionTree tree = info.get().getTree();
            if (!tree.isCycle()) {
                if (tree.isEmpty()) {
                    onCompleted();
                    logger.info("Last message received");
                } else {
                    rxTree = tree;
                }
            }
        }
    }

    public void onCompleted() {
        subject.onCompleted();
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
