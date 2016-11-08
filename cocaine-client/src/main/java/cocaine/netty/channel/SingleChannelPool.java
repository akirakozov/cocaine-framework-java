package cocaine.netty.channel;

import cocaine.CocaineException;
import cocaine.locator.Locator;
import cocaine.netty.ServiceMessageHandler;
import cocaine.session.Sessions;
import com.google.common.base.Supplier;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.pool.ChannelPool;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.Promise;
import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;

import java.net.SocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author metal
 */
public class SingleChannelPool implements ChannelPool {
    private static final Logger logger = Logger.getLogger(SingleChannelPool.class);

    private static final long DEFAULT_SERVICE_AVAILABILITY_TIMEOUT_IN_MS = 1000;

    private final String name;
    private final Sessions sessions;

    private final EventLoopGroup eventLoopGroup;
    private final ChannelInitializer<Channel> channelInitializer;

    private final Supplier<SocketAddress> endpoint;

    private AtomicBoolean closed;
    private volatile CountDownLatch channelLatch = new CountDownLatch(1);
    private Channel channel = null;

    public SingleChannelPool(String name, Sessions sessions, EventLoopGroup eventLoopGroup,
            ChannelInitializer<Channel> channelInitializer, Supplier<SocketAddress> endpoint)
    {
        this.name = name;
        this.sessions = sessions;
        this.eventLoopGroup = eventLoopGroup;
        this.channelInitializer = channelInitializer;
        this.endpoint = endpoint;
        this.closed = new AtomicBoolean(false);
    }

    @Override
    public Future<Channel> acquire() {
        if (channel == null) {
            connect();
        }
        return eventLoopGroup.next().newSucceededFuture(channel);
    }

    @Override
    public Future<Channel> acquire(Promise<Channel> promise) {
        throw new NotImplementedException("SingleChannelPool.acquire(Promise<Channel> promise)");
    }

    @Override
    public Future<Void> release(Channel channel) {
        return eventLoopGroup.next().newSucceededFuture(null);
    }

    @Override
    public Future<Void> release(Channel channel, Promise<Void> promise) {
        return eventLoopGroup.next().newSucceededFuture(null);
    }

    @Override
    public void close() {
        closed.compareAndSet(false, true);
    }

    private void connect() {
        try {
            Bootstrap bootstrap = Locator.createBootstrap(eventLoopGroup, channelInitializer);
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
                                new Thread(() -> connect()).start();
                            }
                        });
                    } else {
                        throw new CocaineException("Couldn't connect to " + endpoint.get());
                    }
                }
            });

            waitForServiceAvailability();

            logger.info("Service " + name + " connected successfully");
        } catch (Exception e) {
            throw new CocaineException(e);
        }
    }

    private void waitForServiceAvailability() {
        try {
            if (!channelLatch.await(DEFAULT_SERVICE_AVAILABILITY_TIMEOUT_IN_MS, TimeUnit.MILLISECONDS)) {
                throw new CocaineException("Service " + name + " unavailable");
            }
        } catch (InterruptedException e) {
            throw new CocaineException("Connection to service " + name
                    + " interrupted while establishing");
        }
    }
}
