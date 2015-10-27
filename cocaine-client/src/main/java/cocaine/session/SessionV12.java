package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.session.protocol.CocaineProtocol;
import io.netty.channel.Channel;

/**
 * @author akirakozov
 */
public class SessionV12<T> implements AutoCloseable {
    private final long id;
    private final ReceiveChannel rx;
    private final TransmitChannel tx;
    private final SessionsV12 sessionsV12;

    public SessionV12(
            long id, String serviceName,
            TransactionTree rx, TransactionTree tx,
            CocaineProtocol protocol,
            SessionsV12 sessionsV12,
            Channel channel,
            CocainePayloadDeserializer deserializer)
    {
        this.id = id;
        this.rx = new ReceiveChannel(serviceName, rx, protocol, deserializer);
        this.tx = new TransmitChannel(serviceName, tx, channel, id);
        this.sessionsV12 = sessionsV12;
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

    @Override
    public void close() throws Exception {
        sessionsV12.removeSession(id);
        onCompleted();
    }

    public void onCompleted() {
        rx.onCompleted();
        tx.onCompleted();
    }
}
