package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.api.TransactionTree.TransactionInfo;
import org.msgpack.type.Value;
import rx.subjects.Subject;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class ReceiveChannel {
    private TransactionTree rxTree;
    private final Subject<Value, Value> subject;
    private final AtomicBoolean isDone;

    public ReceiveChannel(TransactionTree rxTree, Subject<Value, Value> subject) {
        this.rxTree = rxTree;
        this.subject = subject;
        this.isDone = new AtomicBoolean(false);
    }

    public Value get() {
        return subject.toBlocking().first();
    }

    public void onRead(int type, Value payload) {
        TransactionInfo info = rxTree.getInfo(type);
        subject.onNext(payload);
        if (info == null) {
            // TODO: handle incorrect receive message
        }
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
}
