package cocaine.session.protocol;

import cocaine.ServiceException;
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

    public Value handle(String service, String messageType, Value paylod) {
        if (WRITE.equals(messageType)) {
            return paylod;
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
