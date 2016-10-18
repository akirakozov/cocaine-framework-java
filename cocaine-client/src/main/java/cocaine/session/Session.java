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

    private final Channel channel;
    private final Runnable closeChannelCallback;

    public Session(
            long id, String serviceName,
            TransactionTree rx, TransactionTree tx,
            long readTimeoutInMs,
            CocaineProtocol protocol,
            Sessions sessions,
            Channel channel,
            Runnable closeChannelCallback,
            CocainePayloadDeserializer deserializer)
    {
        this.channel = channel;
        this.closeChannelCallback = closeChannelCallback;
        this.id = id;
        this.rx = new ReceiveChannel(serviceName, rx, protocol, deserializer, readTimeoutInMs);
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

    public Channel getChannel() {
        return channel;
    }

    @Override
    public void close() throws Exception {
        sessions.removeSession(id);
        onCompleted();
        closeChannelCallback.run();
    }

    public void onCompleted() {
        rx.onCompleted();
        tx.onCompleted();
        closeChannelCallback.run();
    }
}
