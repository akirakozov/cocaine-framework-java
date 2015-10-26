package cocaine.session.protocol;

import cocaine.ServiceException;
import org.msgpack.type.Value;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author akirakozov
 */
public class PrimitiveProtocol implements CocaineProtocol {
    private static final String VALUE = "value";
    private static final String ERROR = "error";

    public Value handle(String service, String messageType, Value paylod) {
        if (VALUE.equals(messageType)) {
            return paylod;
        } else if (ERROR.equals(messageType)) {
            throw new ServiceException(service, paylod.toString());
        } else {
            throw new ProtocolException(service, "Incorrect message type: " + messageType);
        }
    }

    @Override
    public Set<String> getAllMessageTypes() {
        return new HashSet<>(Arrays.asList(VALUE, ERROR));
    }
}
