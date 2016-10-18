package cocaine.netty.channel;

import cocaine.service.ServiceSpecification;
import cocaine.session.Sessions;
import io.netty.channel.pool.ChannelPool;

/**
 * @author metal
 */
public interface ChannelPoolFactory {
    ChannelPool getChannelPool(ServiceSpecification serviceSpecification, Sessions sessions, int maxNumberOfChannels);
}
