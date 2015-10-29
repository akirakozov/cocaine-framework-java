package cocaine.session.protocol;

import cocaine.ServiceException;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author akirakozov
 */
public class StreamingProtocol implements CocaineProtocol {
    private static final String WRITE = "write";
    private static final String ERROR = "error";
    private static final String CLOSE = "close";

    private static StreamingProtocol instance = new StreamingProtocol();

    private StreamingProtocol() {
    }

    public static StreamingProtocol instance() {
        return instance;
    }

    public Value handle(String service, String messageType, Value paylod) {
        if (!paylod.isArrayValue()) {
            throw new ServiceException(service, "Incorrect payload format: " + paylod.getType());
        }

        if (WRITE.equals(messageType)) {
            ArrayValue values = paylod.asArrayValue();
            return values.size() == 1 ? values.get(0) : paylod;
        } else if (ERROR.equals(messageType)) {
            throw new ServiceException(service, paylod.toString());
        } else if (CLOSE.equals(messageType)) {
            // TODO: return empty object?
            return null;
        } else {
            throw new ProtocolException(service, "Incorrect message type: " + messageType);
        }
    }

    @Override
    public Set<String> getAllMessageTypes() {
        return new HashSet<>(Arrays.asList(WRITE, ERROR, CLOSE));
    }
}
