package cocaine.locator;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import cocaine.ServiceInfo;
import cocaine.api.ServiceApi;
import cocaine.api.TransactionTree;
import cocaine.msgpack.ServiceInfoTemplate;
import cocaine.netty.MessageDecoder;
import cocaine.netty.MessagePackableEncoder;
import cocaine.service.Service;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import cocaine.session.Session;
import com.google.common.base.Suppliers;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.apache.log4j.Logger;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Locator implements AutoCloseable {

    private static final SocketAddress localhost = new InetSocketAddress("localhost", 10053);
    private static final Logger logger = Logger.getLogger(Locator.class);

    private final SocketAddress endpoint;
    private final EventLoopGroup eventLoop;
    private final Bootstrap bootstrap;
    private final Service service;

    private Locator(SocketAddress endpoint, MessagePack pack) {
        this.endpoint = endpoint;
        this.eventLoop = new NioEventLoopGroup(1);
        this.bootstrap = new Bootstrap()
                .group(eventLoop)

                .channel(NioSocketChannel.class)

                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, (int) TimeUnit.SECONDS.toMillis(4))
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.SO_KEEPALIVE, true)

                .handler(new ChannelInitializer<Channel>() {
                    public void initChannel(Channel channel) {
                        channel.pipeline()
                                .addLast("Message Decoder", new MessageDecoder(pack))
                                .addLast("Message Packable Encoder", new MessagePackableEncoder(pack));
                    }
                });

        ServiceApi locatorApi = createLocatorApi();
        this.service = Service.create("locator", bootstrap, Suppliers.ofInstance(endpoint), locatorApi);
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

    public Service service(final String name) {
        return createService(name, Optional.empty());
    }

    public Service service(final String name, final CocaineProtocolsRegistry registry) {
        return createService(name, Optional.of(registry));
    }


    @Override
    public void close() {
        logger.info("Shutting down locator");
        eventLoop.shutdownGracefully();
    }

    private Service createService(final String name, final Optional<CocaineProtocolsRegistry> registry) {
        logger.info("Creating service " + name);
        // TODO: use all endpoints instead of only first
        ServiceInfo info = resolve(name);
        if (registry.isPresent()) {
            return Service.create(name, bootstrap, () -> info.getEndpoints().get(0), info.getApi(), registry.get());
        } else {
            return Service.create(name, bootstrap, () -> info.getEndpoints().get(0), info.getApi());
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
