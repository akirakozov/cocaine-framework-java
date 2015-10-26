package cocaine.session.protocol;

import cocaine.api.TransactionTree;

/**
 * @author akirakozov
 */
public interface CocaineProtocolsRegistry {

    CocaineProtocol findProtocol(TransactionTree rxTree);
}
