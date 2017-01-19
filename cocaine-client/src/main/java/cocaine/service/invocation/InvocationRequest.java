package cocaine.service.invocation;

import cocaine.hpack.HeaderField;
import com.google.common.base.Joiner;

import java.util.List;

/**
 * @author metal
 */
public class InvocationRequest {
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
