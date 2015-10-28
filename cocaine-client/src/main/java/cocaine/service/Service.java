package cocaine.service;

import cocaine.CocaineException;
import cocaine.api.ServiceApi;
import cocaine.netty.ServiceMessageHandler;
import cocaine.session.*;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import cocaine.session.protocol.DefaultCocaineProtocolRegistry;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class Service implements  AutoCloseable {
    private static final Logger logger = Logger.getLogger(Service.class);

    private final String name;
    private final ServiceApi api;
    private final Sessions sessions;

    private AtomicBoolean closed;
    private Channel channel;

    private Service(
            String name, ServiceApi api, Bootstrap bootstrap,
            Supplier<SocketAddress> endpoint, CocaineProtocolsRegistry protocolsRegistry)
    {
        this.name = name;
        this.sessions = new Sessions(name, protocolsRegistry);
        this.api = api;
        this.closed = new AtomicBoolean(false);
        connect(bootstrap, endpoint, new ServiceMessageHandler(name, sessions));
    }

    private Service(String name, ServiceApi api, Bootstrap bootstrap, Supplier<SocketAddress> endpoint) {
        this(name, api, bootstrap, endpoint, DefaultCocaineProtocolRegistry.getDefaultRegistry());
    }

    public static Service create(
            String name, Bootstrap bootstrap, Supplier<SocketAddress> endpoint,
            ServiceApi api, CocaineProtocolsRegistry protocolsRegistry)
    {
        return new Service(name, api, bootstrap, endpoint, protocolsRegistry);
    }

    public static Service create(
            String name, Bootstrap bootstrap,
            Supplier<SocketAddress> endpoint, ServiceApi api)
    {
        return new Service(name, api, bootstrap, endpoint);
    }

    public Session<Value> invoke(String method, List<Object> args) {
        return invoke(method, new ValueIdentityPayloadDeserializer(), args);
    }

    public <T> Session<T> invoke(
            String method, CocainePayloadDeserializer<T> deserializer, List<Object> args)
    {
        Session<T> session = sessions.create(
                api.getReceiveTree(method),
                api.getTransmitTree(method),
                channel,
                deserializer);
        InvocationUtils.invoke(channel, session.getId(), api.getMessageId(method), args);

        return session;
    }

    @Override
    public void close() throws Exception {
        if (closed.compareAndSet(false, true)) {
            channel.close();
        }
    }

    @Override
    public String toString() {
        return name + "/" + channel.remoteAddress();
    }

    private void connect(final Bootstrap bootstrap, final Supplier<SocketAddress> endpoint,
                         final ServiceMessageHandler handler)
    {
        try {
            channel = bootstrap.connect(endpoint.get()).sync().channel();
            channel.pipeline().addLast(handler);
            channel.closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(final ChannelFuture future) throws Exception {
                    future.channel().eventLoop().schedule(
                            () -> {
                                if (!closed.get() && !bootstrap.group().isShuttingDown()) {
                                    connect(bootstrap, endpoint, handler);
                                }
                            }, 2, TimeUnit.SECONDS);
                }
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CocaineException(e);
        }
    }

}
