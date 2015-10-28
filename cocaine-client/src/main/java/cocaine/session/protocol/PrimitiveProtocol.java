package cocaine.session.protocol;

import cocaine.ServiceException;
import com.google.common.base.Preconditions;
import org.msgpack.type.ArrayValue;
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
        if (!paylod.isArrayValue()) {
            throw new ServiceException(service, "Incorrect payload format: " + paylod.getType());
        }

        if (VALUE.equals(messageType)) {
            ArrayValue values = paylod.asArrayValue();
            return values.size() == 1 ? values.get(0) : paylod;
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
