package cocaine.locator;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cocaine.ServiceInfoV12;
import cocaine.api.ServiceApiV12;
import cocaine.api.TransactionTree;
import cocaine.message.Message;
import cocaine.msgpack.MessageTemplate;
import cocaine.msgpack.ServiceInfoV12Template;
import cocaine.netty.MessageDecoder;
import cocaine.netty.MessageEncoder;
import cocaine.netty.MessagePackableEncoder;
import cocaine.service.ServiceV12;
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
    private final MessagePack pack;
    private final Bootstrap bootstrap;
    private final ServiceV12 service;

    private Locator(SocketAddress endpoint) {
        this.endpoint = endpoint;
        this.eventLoop = new NioEventLoopGroup(1);
        this.pack = new MessagePack();
        this.pack.register(Message.class, MessageTemplate.getInstance());
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
                                .addLast("Message Encoder", new MessageEncoder(pack))
                                .addLast("Message Packable Encoder", new MessagePackableEncoder(pack));
                    }
                });

        ServiceApiV12 locatorApi = createLocatorApi();
        this.service = ServiceV12.create("locator", bootstrap, Suppliers.ofInstance(endpoint), locatorApi);
    }

    public static Locator create() {
        return create(localhost);
    }

    public static Locator create(SocketAddress endpoint) {
        logger.info("Creating locator " + endpoint);
        return new Locator(endpoint);
    }

    public ServiceV12 service(final String name) {
        logger.info("Creating service " + name);
        // TODO: use all endpoints instead of only first
        ServiceInfoV12 info = resolve(name);
        return ServiceV12.create(name, bootstrap, () -> info.getEndpoints().get(0), info.getApi());
    }

    @Override
    public void close() {
        logger.info("Shutting down locator");
        eventLoop.shutdownGracefully();
    }

    private ServiceInfoV12 resolve(String name) {
        logger.info("Resolving service info for " + name);
        try {
            Value paylaod = service.invoke("resolve", name).rx().get();
            return new Converter(paylaod).read(ServiceInfoV12Template.create(name));
            //byte[] result = service.invoke("resolve", name).toBlocking().single();
            //return pack.read(result, ServiceInfoTemplate.create(name));
        } catch (Exception e) {
            throw new LocatorResolveException(name, endpoint, e);
        }
    }

    private static ServiceApiV12 createLocatorApi() {
        // {0: ['resolve', {}, {0: ['value', {}], 1: ['error', {}]}]
        // TODO: add another methods
        Map<Integer, ServiceApiV12.TransactionDescription> map = new HashMap();
        map.put(0, new ServiceApiV12.TransactionDescription(
                "resolve", TransactionTree.SIMPLE_VALUE, TransactionTree.EMPTY));

        return new ServiceApiV12(map);
    }

}
