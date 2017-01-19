package cocaine.locator;

import cocaine.ServiceInfo;
import cocaine.api.ServiceApi;
import cocaine.api.TransactionTree;
import cocaine.msgpack.ServiceInfoTemplate;
import cocaine.service.Service;
import cocaine.service.ServiceOptions;
import cocaine.service.ServiceSpecification;
import cocaine.service.invocation.AdditionalHeadersAppender;
import cocaine.service.invocation.IdentityHeadersAppender;
import cocaine.session.Session;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import cocaine.session.protocol.DefaultCocaineProtocolRegistry;
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

        ServiceSpecification specs = new ServiceSpecification("locator", Suppliers.ofInstance(endpoint),
                createSeparateEventLoop(), pack, DefaultCocaineProtocolRegistry.getDefaultRegistry());
        ServiceOptions options = new ServiceOptions();
        this.service = Service.create(createLocatorApi(), specs, options);
    }

    public static Bootstrap createBootstrap(EventLoopGroup eventLoopGroup, ChannelInitializer<Channel> channelInitializer)
    {
        return createBootstrap(eventLoopGroup).handler(channelInitializer);
    }

    public static Bootstrap createBootstrap(EventLoopGroup eventLoopGroup) {
        return new Bootstrap()
                .group(eventLoopGroup)

                .channel(NioSocketChannel.class)

                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(4))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true);
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

    public Service service(final String name, final ServiceOptions options, final AdditionalHeadersAppender appender) {
        return createService(name, options, DefaultCocaineProtocolRegistry.getDefaultRegistry(), appender);
    }

    public Service service(final String name, final ServiceOptions options, final CocaineProtocolsRegistry registry) {
        return createService(name, options, registry, new IdentityHeadersAppender());
    }

    public Service service(final String name, final ServiceOptions options, final CocaineProtocolsRegistry registry,
            final AdditionalHeadersAppender appender)
    {
        return createService(name, options, registry, appender);
    }

    @Override
    public void close() {
        logger.info("Shutting down locator");
        service.close();
        eventLoops.forEach(EventExecutorGroup::shutdownGracefully);
    }

    private EventLoopGroup createSeparateEventLoop() {
        EventLoopGroup eventLoop = new NioEventLoopGroup(1);
        eventLoops.add(eventLoop);
        return eventLoop;
    }

    private Service createService(final String name, final ServiceOptions options,
            final CocaineProtocolsRegistry registry, final AdditionalHeadersAppender appender)
    {
        logger.info("Creating service " + name);
        // TODO: use all endpoints instead of only first
        ServiceInfo info = resolve(name);
        ServiceSpecification specs = new ServiceSpecification(name, () -> info.getEndpoints().get(0),
                createSeparateEventLoop(), pack, registry);
        return Service.create(info.getApi(), specs, options, appender);
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
