package cocaine.service;

import cocaine.request.RequestIdStack;
import com.google.common.base.Joiner;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;
import org.msgpack.MessagePackable;
import org.msgpack.packer.Packer;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author akirakozov
 */
public class InvocationUtils {
    private static final Logger logger = Logger.getLogger(InvocationUtils.class);

    public static void invoke(Channel channel, long sessionId, int method, List<Object> args) {
        channel
                .write(new InvocationRequest(method, sessionId, args, constructRequestIdHeaders()))
                .addListener(errorLoggingListener(sessionId, method));
    }

    public static void invokeAndFlush(Channel channel, long sessionId, int method, List<Object> args) {
        channel
                .writeAndFlush(new InvocationRequest(method, sessionId, args, constructRequestIdHeaders()))
                .addListener(errorLoggingListener(sessionId, method));
    }

    private static ChannelFutureListener errorLoggingListener(long sessionId, int method) {
        return future -> {
            if (future.isDone() && future.cause() != null) {
                logger.error("Invoking method " + method + " in session " + sessionId + " failed", future.cause());
            }
        };
    }

    private static List<List<Object>> constructRequestIdHeaders() {
        List<List<Object>> result = new ArrayList<>();
        if (RequestIdStack.hasAllIds()) {
            RequestIdStack.AVAILABLE_IDS.forEach(type -> {
                byte[] id = RequestIdStack.currentId(type);
                result.add(constructRequestIdHeader(type, id));
            });
        }
        return result;
    }

    private static List<Object> constructRequestIdHeader(RequestIdStack.Type type, byte[] value) {
        List<Object> result = new ArrayList<>();
        result.add(false);
        result.add(type.getHeaderIndex());
        result.add(value);
        return result;
    }

    private static class InvocationRequest implements MessagePackable {

        private final int method;
        private final long session;
        private final List<Object> args;

        private final List<List<Object>> headers;

        public InvocationRequest(int method, long session, List<Object> args, List<List<Object>> headers) {
            this.method = method;
            this.session = session;
            this.args = args;
            this.headers = headers;
        }

        @Override
        public void writeTo(Packer packer) throws IOException {
            packer.writeArrayBegin(headers.isEmpty() ? 3 : 4);
            packer.write(session);
            packer.write(method);
            packer.write(args);
            if (!headers.isEmpty()) {
                packer.write(headers);
            }
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
