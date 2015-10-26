package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.session.protocol.CocaineProtocol;

/**
 * @author akirakozov
 */
public class SessionV12<T> {
    private final long id;
    private final ReceiveChannel rx;
    private final TransmitChannel tx;

    public SessionV12(
            long id, String serviceName,
            TransactionTree rx, TransactionTree tx,
            CocaineProtocol protocol,
            CocainePayloadDeserializer deserializer)
    {
        this.id = id;
        this.rx = new ReceiveChannel(serviceName, rx, protocol, deserializer);
        this.tx = new TransmitChannel(tx);
    }

    public ReceiveChannel<T> rx() {
        return rx;
    }

    public TransmitChannel tx() {
        return tx;
    }

    public long getId() {
        return id;
    }
}
