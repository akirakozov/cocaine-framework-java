package cocaine.service;

import cocaine.hpack.HeaderField;
import cocaine.request.RequestIdStack;
import com.google.common.base.Joiner;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import org.apache.log4j.Logger;

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

    private static List<HeaderField> constructRequestIdHeaders() {
        List<HeaderField> headers = new ArrayList<>();
        if (!RequestIdStack.isEmpty()) {
            RequestIdStack.State currentIds = RequestIdStack.current.get();
            headers.add(new HeaderField("trace_id".getBytes(), HeaderField.valueFromLong(currentIds.traceId)));
            headers.add(new HeaderField("span_id".getBytes(), HeaderField.valueFromLong(currentIds.spanId)));
            headers.add(new HeaderField("parent_id".getBytes(), HeaderField.valueFromLong(currentIds.parentId)));
        }
        return headers;
    }

    public static class InvocationRequest {
        public final int method;
        public final long session;
        public final List<Object> args;

        public final List<HeaderField> headers;

        public InvocationRequest(int method, long session, List<Object> args, List<HeaderField> headers) {
            this.method = method;
            this.session = session;
            this.args = args;
            this.headers = headers;
        }

        @Override
        public String toString() {
            return "InvocationRequest/" + session + ": " + method + " [" + Joiner.on(", ").join(args) + "]";
        }
    }
}
