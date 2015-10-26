package cocaine.service;

import cocaine.CocaineException;
import cocaine.api.ServiceApiV12;
import cocaine.netty.ServiceMessageHandlerV12;
import cocaine.session.*;
import cocaine.session.protocol.CocaineProtocol;
import com.google.common.base.Joiner;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.net.SocketAddress;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class ServiceV12 implements  AutoCloseable {
    private static final Logger logger = Logger.getLogger(ServiceV12.class);

    private final String name;
    private final ServiceApiV12 api;
    private final SessionsV12 sessions;

    private AtomicBoolean closed;
    private Channel channel;

    private ServiceV12(String name, ServiceApiV12 api, Bootstrap bootstrap, Supplier<SocketAddress> endpoint) {
        this.name = name;
        this.sessions = new SessionsV12(name);
        this.api = api;
        this.closed = new AtomicBoolean(false);
        connect(bootstrap, endpoint, new ServiceMessageHandlerV12(name, sessions));
    }

    public static ServiceV12 create(String name, Bootstrap bootstrap, Supplier<SocketAddress> endpoint, ServiceApiV12 api) {
        return new ServiceV12(name, api, bootstrap, endpoint);
    }

    public SessionV12<Value> invoke(String method, CocaineProtocol protocol, Object... args) {
        return invoke(method, Arrays.asList(args), protocol);
    }

    public SessionV12<Value> invoke(String method, List<Object> args, CocaineProtocol protocol) {
        return invoke(method, args, protocol, new ValueIdentityPayloadDeserializer());
    }

    public <T> SessionV12<T> invoke(
            String method, List<Object> args,
            CocaineProtocol protocol, CocainePayloadDeserializer<T> deserializer)
    {
        logger.debug("Invoking " + method + "(" + Joiner.on(", ").join(args) + ") asynchronously");

        SessionV12<T> session = sessions.create(
                api.getReceiveTree(method),
                api.getTransmitTree(method),
                protocol,
                deserializer);
        int requestedMethod = api.getMessageId(method);
        channel.write(new InvocationRequest(requestedMethod, session.getId(), args));

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
                         final ServiceMessageHandlerV12 handler)
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

    private static class InvocationRequest implements MessagePackable {

        private final int method;
        private final long session;
        private final List<Object> args;

        public InvocationRequest(int method, long session, List<Object> args) {
            this.method = method;
            this.session = session;
            this.args = args;
        }

        @Override
        public void writeTo(Packer packer) throws IOException {
            packer.writeArrayBegin(3);
            packer.write(session);
            packer.write(method);
            packer.write(args);
            packer.writeArrayEnd();
        }

        @Override
        public void readFrom(Unpacker unpacker) {
            throw new UnsupportedOperationException("Reading InvocationRequest is not supported");
        }

        @Override
        public String toString() {
            return "InvocationRequest/" + session + ": " + method + " [" + Joiner.on(", ").join(args) + "]";
        }
    }

}
