package cocaine.service;

import com.google.common.base.Joiner;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.List;

/**
 * @author akirakozov
 */
public class InvocationUtils {
    private static final Logger logger = Logger.getLogger(InvocationUtils.class);

    public static void invoke(Channel channel, long sessionId, int method, List<Object> args) {
        channel
                .write(new InvocationRequest(method, sessionId, args))
                .addListener(errorLoggingListener(sessionId, method));
    }

    public static void invokeAndFlush(Channel channel, long sessionId, int method, List<Object> args) {
        channel
                .writeAndFlush(new InvocationRequest(method, sessionId, args))
                .addListener(errorLoggingListener(sessionId, method));
    }

    private static ChannelFutureListener errorLoggingListener(long sessionId, int method) {
        return future -> {
            if (future.isDone() && future.cause() != null) {
                logger.error("Invoking method " + method + " in session " + sessionId + " failed", future.cause());
            }
        };
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
