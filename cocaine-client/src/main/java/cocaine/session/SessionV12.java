package cocaine.session;

import cocaine.api.TransactionTree;
import org.msgpack.type.Value;
import rx.subjects.Subject;

/**
 * @author akirakozov
 */
public class SessionV12 {
    private final long id;
    private final ReceiveChannel rx;
    private final TransmitChannel tx;

    public SessionV12(long id, TransactionTree rx, TransactionTree tx, Subject<Value, Value> subject) {
        this.id = id;
        this.rx = new ReceiveChannel(rx, subject);
        this.tx = new TransmitChannel(tx);
    }

    public ReceiveChannel rx() {
        return rx;
    }

    public TransmitChannel tx() {
        return tx;
    }

    public long getId() {
        return id;
    }
}
