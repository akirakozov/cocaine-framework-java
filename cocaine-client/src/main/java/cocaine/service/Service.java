package cocaine.service;

import cocaine.CocaineException;
import cocaine.api.ServiceApi;
import cocaine.api.TransactionTree;
import cocaine.netty.channel.CocaineChannelPoolFactory;
import cocaine.session.*;
import cocaine.session.protocol.PrimitiveProtocol;
import io.netty.channel.*;
import io.netty.channel.pool.ChannelPool;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class Service implements AutoCloseable {
    private static final Logger logger = Logger.getLogger(Service.class);

    private final ServiceApi api;
    private final Sessions sessions;

    private final ChannelPool channelPool;

    private AtomicBoolean closed;
    private final ServiceOptions options;

    private Service(ServiceApi api, ServiceSpecification specs, ServiceOptions options)
    {
        this.sessions = new Sessions(specs.name, options.readTimeoutInMs, specs.protocolsRegistry);
        this.api = api;
        this.channelPool = new CocaineChannelPoolFactory()
                .getChannelPool(specs, sessions, options.maxNumberOfOpenChannels);

        this.closed = new AtomicBoolean(false);
        this.options = options;
    }

    public static Service create(ServiceApi api, ServiceSpecification specs, ServiceOptions options) {
        return new Service(api, specs, options);
    }

    public Session<Value> invoke(String method, List<Object> args) {
        return invoke(method, new ValueIdentityPayloadDeserializer(), args);
    }

    public <T> Session<T> invoke(String method, CocainePayloadDeserializer<T> deserializer, List<Object> args) {
        Channel sessionChannel = getChannel();
        Session<T> session = sessions.create(
                api.getReceiveTree(method),
                api.getTransmitTree(method),
                sessionChannel,
                channelPool::release,
                deserializer);
        if (options.immediatelyFlushAllInvocations) {
            InvocationUtils.invokeAndFlush(session.getChannel(), session.getId(), api.getMessageId(method), args);
        } else {
            InvocationUtils.invoke(session.getChannel(), session.getId(), api.getMessageId(method), args);
        }

        return session;
    }

    public boolean isPrimitiveOrIdentityApiMethod(String method) {
        TransactionTree rxTree = api.getReceiveTree(method);
        TransactionTree txTree = api.getTransmitTree(method);
        if (rxTree.isEmpty() && txTree.isEmpty()) {
            return true;
        }

        boolean isPrimitiveRxProtocol = Objects.equals(
                PrimitiveProtocol.instance().getAllMessageTypes(),
                rxTree.getAllMessageTypes());
        return isPrimitiveRxProtocol && txTree.isEmpty();
    }

    private Channel getChannel() {
        try {
            return channelPool.acquire().get();
        } catch (Exception e) {
            throw new CocaineException("Channel acquiring failed", e);
        }
    }

    @Override
    public void close() {
        logger.info("Closing service " + toString() + " and it's channel");

        if (closed.compareAndSet(false, true)) {
            channelPool.close();
        }
    }

    @Override
    public String toString() {
        //TODO: better service identifier (was "/" + channel.remoteAddress())
        return sessions.getService();
    }
}
