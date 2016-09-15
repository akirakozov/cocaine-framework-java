package cocaine.service;

import cocaine.CocaineException;
import cocaine.api.ServiceApi;
import cocaine.api.TransactionTree;
import cocaine.netty.ServiceMessageHandler;
import cocaine.session.*;
import cocaine.session.protocol.CocaineProtocolsRegistry;
import cocaine.session.protocol.DefaultCocaineProtocolRegistry;
import cocaine.session.protocol.PrimitiveProtocol;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.msgpack.type.Value;

import java.net.SocketAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author akirakozov
 */
public class Service implements  AutoCloseable {
    private static final Logger logger = Logger.getLogger(Service.class);
    private static final long DEFAULT_SERVICE_AVAILABILITY_TIMEOUT_IN_MS = 60000;

    private final String name;
    private final ServiceApi api;
    private final Sessions sessions;

    private AtomicBoolean closed;
    private volatile CountDownLatch channelLatch = new CountDownLatch(1);
    private Channel channel;

    private boolean immediatelyFlushAllInvocations;

    private Service(
            String name, ServiceApi api, Bootstrap bootstrap,
            Supplier<SocketAddress> endpoint, long readTimeout, boolean immediatelyFlushAllInvocations,
            CocaineProtocolsRegistry protocolsRegistry)
    {
        this.name = name;
        this.sessions = new Sessions(name, readTimeout, protocolsRegistry);
        this.api = api;
        this.closed = new AtomicBoolean(false);
        this.immediatelyFlushAllInvocations = immediatelyFlushAllInvocations;
        connect(bootstrap, endpoint);
    }

    private Service(String name, ServiceApi api, Bootstrap bootstrap, Supplier<SocketAddress> endpoint,
            long readTimeout, boolean immediatelyFlushAllInvocations)
    {
        this(name, api, bootstrap, endpoint, readTimeout, immediatelyFlushAllInvocations,
                DefaultCocaineProtocolRegistry.getDefaultRegistry());
    }

    public static Service create(
            String name, Bootstrap bootstrap, Supplier<SocketAddress> endpoint,
            long readTimeout, boolean immediatelyFlushAllInvocations,
            ServiceApi api, CocaineProtocolsRegistry protocolsRegistry)
    {
        return new Service(name, api, bootstrap, endpoint, readTimeout, immediatelyFlushAllInvocations,
                protocolsRegistry);
    }

    public static Service create(
            String name, Bootstrap bootstrap,
            Supplier<SocketAddress> endpoint, long readTimeout, boolean immediatelyFlushAllInvocations, ServiceApi api)
    {
        return new Service(name, api, bootstrap, endpoint, readTimeout, immediatelyFlushAllInvocations);
    }

    public Session<Value> invoke(String method, List<Object> args) {
        return invoke(method, new ValueIdentityPayloadDeserializer(), args);
    }

    public <T> Session<T> invoke(String method, CocainePayloadDeserializer<T> deserializer, List<Object> args) {
        waitForServiceAvailability(Operation.INVOCATION);

        Session<T> session = sessions.create(
                api.getReceiveTree(method),
                api.getTransmitTree(method),
                channel,
                deserializer);
        if (immediatelyFlushAllInvocations) {
            InvocationUtils.invokeAndFlush(channel, session.getId(), api.getMessageId(method), args);
        } else {
            InvocationUtils.invoke(channel, session.getId(), api.getMessageId(method), args);
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

    @Override
    public void close() throws Exception {
        logger.info("Closing service " + toString() + " and it's channel");

        if (closed.compareAndSet(false, true)) {
            channel.close();
        }
    }

    @Override
    public String toString() {
        return name + "/" + channel.remoteAddress();
    }

    private void connect(final Bootstrap bootstrap, final Supplier<SocketAddress> endpoint) {
        try {
            ChannelFuture connectFuture = bootstrap.connect(endpoint.get());
            connectFuture.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture cf) throws Exception {
                    if (cf.isSuccess()) {
                        channel = cf.channel();
                        channelLatch.countDown();

                        channel.pipeline().addLast(new ServiceMessageHandler(name, sessions));
                        channel.closeFuture().addListener((ChannelFutureListener) future -> {
                            if (!closed.get() && !bootstrap.group().isShuttingDown()) {
                                sessions.onCompleted();
                                channelLatch = new CountDownLatch(1);
                                new Thread(() -> connect(bootstrap, endpoint)).start();
                            }
                        });
                    } else {
                        throw new CocaineException("Couldn't connect to " + endpoint.get());
                    }
                }
            });

            waitForServiceAvailability(Operation.CONNECTION);

            logger.info("Service " + name + " connected successfully");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new CocaineException(e);
        }
    }

    private void waitForServiceAvailability(Operation operation) {
        try {
            if (!channelLatch.await(DEFAULT_SERVICE_AVAILABILITY_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS)) {
                throw new CocaineException("Service " + name + " unavailable");
            }
        } catch (InterruptedException e) {
            throw new CocaineException(operation.name().toLowerCase() + " for service " + name
                    + " interrupted while waiting for connection to arrive");
        }
    }

    private enum Operation {
        CONNECTION, INVOCATION
    }

}
