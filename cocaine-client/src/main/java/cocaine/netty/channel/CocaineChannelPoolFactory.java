package cocaine.netty.channel;

import cocaine.locator.Locator;
import cocaine.netty.MessageDecoder;
import cocaine.netty.InvocationRequestEncoder;
import cocaine.netty.ServiceMessageHandler;
import cocaine.service.ServiceSpecification;
import cocaine.session.Sessions;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.pool.AbstractChannelPoolHandler;
import io.netty.channel.pool.ChannelPool;
import io.netty.channel.pool.ChannelPoolHandler;
import io.netty.channel.pool.FixedChannelPool;
import org.msgpack.MessagePack;

/**
 * @author metal
 */
public class CocaineChannelPoolFactory implements ChannelPoolFactory {
    @Override
    public ChannelPool getChannelPool(ServiceSpecification serviceSpecification, Sessions sessions,
            int maxNumberOfChannels)
    {
        return maxNumberOfChannels == 1
                ? getSingleChannelPool(serviceSpecification, sessions)
                : getFixedChannelPool(serviceSpecification, sessions, maxNumberOfChannels);
    }

    private ChannelPool getSingleChannelPool(ServiceSpecification specs, Sessions sessions) {
        return new SingleChannelPool(specs.name, sessions, specs.eventLoopGroup,
                createChannelInitializer(specs.pack), specs.endpoint);
    }

    private ChannelPool getFixedChannelPool(ServiceSpecification specs, Sessions sessions,
            int maxNumberOfChannels)
    {
        Bootstrap bootstrap = Locator.createBootstrap(specs.eventLoopGroup).remoteAddress(specs.endpoint.get());
        return new FixedChannelPool(bootstrap, createChannelPoolHandler(sessions, specs.pack), maxNumberOfChannels);
    }

    private static ChannelInitializer<Channel> createChannelInitializer(final MessagePack pack) {
        return new ChannelInitializer<Channel>() {
            public void initChannel(Channel channel) {
                channel.pipeline()
                        .addLast("Message Decoder", new MessageDecoder(pack))
                        .addLast("Message Packable Encoder", new InvocationRequestEncoder(pack));
            }
        };
    }

    private static ChannelPoolHandler createChannelPoolHandler(final Sessions sessions, final MessagePack pack) {
        return new AbstractChannelPoolHandler() {
            @Override
            public void channelCreated(Channel ch) throws Exception {
                ch.pipeline()
                        .addLast("Message Decoder", new MessageDecoder(pack))
                        .addLast("Message Packable Encoder", new InvocationRequestEncoder(pack))
                        .addLast(new ServiceMessageHandler(sessions.getService(), sessions));
            }
        };
    }
}
