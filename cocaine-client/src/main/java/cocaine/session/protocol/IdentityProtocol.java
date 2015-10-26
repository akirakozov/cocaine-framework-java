package cocaine.session.protocol;

import org.msgpack.type.Value;

import java.util.Collections;
import java.util.Set;

/**
 * @author akirakozov
 */
public class IdentityProtocol implements CocaineProtocol {

    @Override
    public Value handle(String service, String messageType, Value paylod) {
        return paylod;
    }

    @Override
    public Set<String> getAllMessageTypes() {
        return Collections.emptySet();
    }
}
