package cocaine.session;

import cocaine.api.TransactionTree;
import cocaine.session.protocol.CocaineProtocol;
import io.netty.channel.Channel;

/**
 * @author akirakozov
 */
public class Session<T> implements AutoCloseable {
    private final long id;
    private final ReceiveChannel rx;
    private final TransmitChannel tx;
    private final Sessions sessions;

    public Session(
            long id, String serviceName,
            TransactionTree rx, TransactionTree tx,
            CocaineProtocol protocol,
            Sessions sessions,
            Channel channel,
            CocainePayloadDeserializer deserializer)
    {
        this.id = id;
        this.rx = new ReceiveChannel(serviceName, rx, protocol, deserializer);
        this.tx = new TransmitChannel(serviceName, tx, channel, id);
        this.sessions = sessions;
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
        sessions.removeSession(id);
        onCompleted();
    }

    public void onCompleted() {
        rx.onCompleted();
        tx.onCompleted();
    }
}
