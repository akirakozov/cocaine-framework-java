package cocaine.locator;

import cocaine.ServiceInfo;
import cocaine.api.ServiceApi;
import cocaine.api.TransactionTree;
import cocaine.msgpack.ServiceInfoTemplate;
import cocaine.netty.MessageDecoder;
import cocaine.netty.MessagePackableEncoder;
import cocaine.service.Service;
import cocaine.session.Session;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import com.google.common.base.Suppliers;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.EventExecutorGroup;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Locator implements AutoCloseable {

    private static final SocketAddress localhost = new InetSocketAddress("localhost", 10053);
    private static final Logger logger = Logger.getLogger(Locator.class);

    private final SocketAddress endpoint;
    private final MessagePack pack;
    private final List<EventLoopGroup> eventLoops;
    private final Service service;

    private Locator(SocketAddress endpoint, MessagePack pack) {
        this.endpoint = endpoint;
        this.pack = pack;
        this.eventLoops = new ArrayList<>();

        this.service = Service.create("locator", createSeparateEventLoop(), createChannelInitializer(pack),
                Suppliers.ofInstance(endpoint), 0, false, createLocatorApi());
    }

    public static Bootstrap createBootstrap(EventLoopGroup eventLoopGroup, ChannelInitializer<Channel> channelInitializer)
    {
        return new Bootstrap()
                .group(eventLoopGroup)

                .channel(NioSocketChannel.class)

                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(4))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)

                .handler(channelInitializer);
    }

    public static Locator create() {
        return create(localhost, new MessagePack());
    }

    public static Locator create(MessagePack pack) {
        return create(localhost, pack);
    }

    public static Locator create(SocketAddress endpoint, MessagePack pack) {
        logger.info("Creating locator " + endpoint);
        return new Locator(endpoint, pack);
    }

    public Service service(final String name, final long readTimeout, final boolean immediatelyFlushAllInvocations) {
        return createService(name, readTimeout, immediatelyFlushAllInvocations, Optional.empty());
    }

    public Service service(final String name, final long readTimeout, final CocaineProtocolsRegistry registry,
            final boolean immediatelyFlushAllInvocations)
    {
        return createService(name, readTimeout, immediatelyFlushAllInvocations, Optional.of(registry));
    }

    @Override
    public void close() {
        logger.info("Shutting down locator");
        eventLoops.forEach(EventExecutorGroup::shutdownGracefully);
    }

    private static ChannelInitializer<Channel> createChannelInitializer(final MessagePack pack) {
        return new ChannelInitializer<Channel>() {
            public void initChannel(Channel channel) {
                channel.pipeline()
                        .addLast("Message Decoder", new MessageDecoder(pack))
                        .addLast("Message Packable Encoder", new MessagePackableEncoder(pack));
            }
        };
    }

    private EventLoopGroup createSeparateEventLoop() {
        EventLoopGroup eventLoop = new NioEventLoopGroup(1);
        eventLoops.add(eventLoop);
        return eventLoop;
    }

    private Service createService(final String name,
            final long readTimeout, final boolean immediatelyFlushAllInvocations,
            final Optional<CocaineProtocolsRegistry> registry)
    {
        logger.info("Creating service " + name);
        // TODO: use all endpoints instead of only first
        ServiceInfo info = resolve(name);
        if (registry.isPresent()) {
            return Service.create(name, createSeparateEventLoop(), createChannelInitializer(pack),
                    () -> info.getEndpoints().get(0), readTimeout, immediatelyFlushAllInvocations, info.getApi(), registry.get());
        } else {
            return Service.create(name, createSeparateEventLoop(), createChannelInitializer(pack),
                    () -> info.getEndpoints().get(0), readTimeout, immediatelyFlushAllInvocations, info.getApi());
        }
    }

    private ServiceInfo resolve(String name) {
        logger.info("Resolving service info for " + name);
        try {
            Session<Value> session = service.invoke("resolve", Arrays.asList(name));
            return new Converter(session.rx().get()).read(ServiceInfoTemplate.create(name));
        } catch (Exception e) {
            throw new LocatorResolveException(name, endpoint, e);
        }
    }

    private static ServiceApi createLocatorApi() {
        // {0: ['resolve', {}, {0: ['value', {}], 1: ['error', {}]}]
        // TODO: add another methods
        Map<Integer, ServiceApi.TransactionDescription> map = new HashMap();
        map.put(0, new ServiceApi.TransactionDescription(
                "resolve", TransactionTree.SIMPLE_VALUE, TransactionTree.EMPTY));

        return new ServiceApi("locator", map);
    }

}
