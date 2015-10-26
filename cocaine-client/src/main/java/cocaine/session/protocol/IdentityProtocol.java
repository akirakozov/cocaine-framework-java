package cocaine.session.protocol;

import org.msgpack.type.Value;

/**
 * @author akirakozov
 */
public class IdentityProtocol implements CocaineProtocol {

    @Override
    public Value handle(String service, String messageType, Value paylod) {
        return paylod;
    }
}
