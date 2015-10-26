package cocaine.session.protocol;

import org.msgpack.type.Value;

import java.util.Set;

/**
 * @author akirakozov
 */
public interface CocaineProtocol {
    Value handle(String service, String messageType, Value paylod);
    Set<String> getAllMessageTypes();
}
