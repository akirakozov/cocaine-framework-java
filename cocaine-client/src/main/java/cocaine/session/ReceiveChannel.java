package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import org.msgpack.type.Value;
import rx.subjects.ReplaySubject;
import rx.subjects.Subject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class ReceiveChannel<T> {
    private TransactionTree rxTree;
    private final Subject<ResultMessage, ResultMessage> subject;
    private final AtomicBoolean isDone;
    private final CocaineProtocol protocol;
    private final String serviceName;

    public ReceiveChannel(
            String serviceName,
            TransactionTree rxTree,
            CocaineProtocol protocol)
    {
        this.serviceName = serviceName;
        this.rxTree = rxTree;
        this.subject = ReplaySubject.create();
        this.isDone = new AtomicBoolean(false);
        this.protocol = protocol;
    }

    public T get() {
        ResultMessage msg = subject.toBlocking().first();
        return (T) protocol.handle(serviceName, msg.messageType, msg.payload);
    }

    void onRead(int type, Value payload) {
        TransactionInfo info = rxTree.getInfo(type);
        if (info == null) {
            // TODO: handle incorrect receive message
        }
        subject.onNext(new ResultMessage(info.getMessageName(), payload));
        if (info.getTree().isEmpty()) {
            done();
        } else {
            rxTree = info.getTree();
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
