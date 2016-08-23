package cocaine.session.protocol;

import cocaine.api.TransactionTree;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * @author akirakozov
 */
public class DefaultCocaineProtocolRegistry implements CocaineProtocolsRegistry {
    private final static DefaultCocaineProtocolRegistry defaultRegistry = createDefaultCocaineRegistry();
    private final List<CocaineProtocol> protocols;

    public DefaultCocaineProtocolRegistry(List<CocaineProtocol> protocols) {
        this.protocols = protocols;
    }

    @Override
    public CocaineProtocol findProtocol(TransactionTree rxTree) {
        Set<String> values = rxTree.getAllMessageTypes();
        return protocols.stream()
                .filter( p -> p.getAllMessageTypes().equals(values))
                .findFirst().orElseGet(IdentityProtocol::instance);
    }

    public static DefaultCocaineProtocolRegistry getDefaultRegistry() {
        return defaultRegistry;
    }

    private static DefaultCocaineProtocolRegistry createDefaultCocaineRegistry() {
        return new DefaultCocaineProtocolRegistry(
                Arrays.asList(
                        PrimitiveProtocol.instance(),
                        StreamingProtocol.instance(),
                        StreamingReadProtocol.instance()));
    }

}
