package cocaine.service;

import cocaine.session.protocol.CocaineProtocolsRegistry;
import com.google.common.base.Supplier;
import io.netty.channel.EventLoopGroup;
import org.msgpack.MessagePack;

import java.net.SocketAddress;

/**
 * @author metal
 */
public class ServiceSpecification {
    public final String name;
    public final Supplier<SocketAddress> endpoint;
    public final EventLoopGroup eventLoopGroup;
    public final MessagePack pack;
    public final CocaineProtocolsRegistry protocolsRegistry;

    public ServiceSpecification(String name, Supplier<SocketAddress> endpoint, EventLoopGroup eventLoopGroup,
            MessagePack pack, CocaineProtocolsRegistry protocolsRegistry)
    {
        this.name = name;
        this.endpoint = endpoint;
        this.eventLoopGroup = eventLoopGroup;
        this.pack = pack;
        this.protocolsRegistry = protocolsRegistry;
    }
}
