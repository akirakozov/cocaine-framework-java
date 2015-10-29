package cocaine.session.protocol;

import org.msgpack.type.Value;

import java.util.Collections;
import java.util.Set;

/**
 * @author akirakozov
 */
public class IdentityProtocol implements CocaineProtocol {

    private static IdentityProtocol instance = new IdentityProtocol();

    private IdentityProtocol() {
    }

    public static IdentityProtocol instance() {
        return instance;
    }

    @Override
    public Value handle(String service, String messageType, Value paylod) {
        return paylod;
    }

    @Override
    public Set<String> getAllMessageTypes() {
        return Collections.emptySet();
    }
}
