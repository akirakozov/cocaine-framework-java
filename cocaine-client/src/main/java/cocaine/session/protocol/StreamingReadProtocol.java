package cocaine.session.protocol;

import cocaine.ServiceException;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author metal
 */
public class StreamingReadProtocol implements CocaineProtocol {
    private static final String META = "meta";
    private static final String CHUNK = "chunk";
    private static final String END = "end";
    private static final String ERROR = "error";

    private static final Set<String> INFORMATIVE_MESSAGES = new HashSet<>(Arrays.asList(META, CHUNK));

    private static StreamingReadProtocol instance = new StreamingReadProtocol();

    private StreamingReadProtocol() {
    }

    public static StreamingReadProtocol instance() {
        return instance;
    }

    public Value handle(String service, String messageType, Value payload) {
        if (!payload.isArrayValue()) {
            throw new ServiceException(service, "Incorrect payload format: " + payload.getType());
        }

        if (INFORMATIVE_MESSAGES.contains(messageType)) {
            ArrayValue values = payload.asArrayValue();
            return values.size() == 1 ? values.get(0) : payload;
        } else if (END.equals(messageType)) {
            return null;
        } else if (ERROR.equals(messageType)) {
            throw new ServiceException(service, payload.toString());
        } else {
            throw new ProtocolException(service, "Incorrect message type: " + messageType);
        }
    }

    @Override
    public Set<String> getAllMessageTypes() {
        return new HashSet<>(Arrays.asList(META, CHUNK, END, ERROR));
    }
}
