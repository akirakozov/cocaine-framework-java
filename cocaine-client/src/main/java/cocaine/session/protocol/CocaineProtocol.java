package cocaine.session.protocol;

import org.msgpack.type.Value;

/**
 * @author akirakozov
 */
public interface CocaineProtocol {
    Value handle(String service, String messageType, Value paylod);
}
