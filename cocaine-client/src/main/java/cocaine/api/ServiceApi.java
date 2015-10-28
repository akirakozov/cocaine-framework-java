package cocaine.api;

import cocaine.UnknownServiceMethodException;

import java.util.Map;

/**
 * @author akirakozov
 */
public class ServiceApi {
    private final String serviceName;
    private final Map<Integer, TransactionDescription> transactions;

    public ServiceApi(String serviceName, Map<Integer, TransactionDescription> transactions) {
        this.serviceName = serviceName;
        this.transactions = transactions;
    }

    public TransactionTree getTransmitTree(String name) {
        return findDescription(name).getTransmitTree();
    }

    public TransactionTree getReceiveTree(String name) {
        return findDescription(name).getReceiveTree();
    }

    public int getMessageId(String name) {
        return transactions.entrySet().stream()
                .filter( it -> it.getValue().getMessageName().equals(name))
                .findFirst()
                .orElseThrow(() -> new UnknownServiceMethodException(serviceName,name))
                .getKey();
    }

    private TransactionDescription findDescription(String name) {
        return transactions.values().stream()
                .filter(info -> info.getMessageName().equals(name))
                .findFirst()
                .orElseThrow(() -> new UnknownServiceMethodException(serviceName,name));
    }

    /**
     * @author akirakozov
     */
    public static class TransactionDescription {

        private final String messageName;
        private final TransactionTree receiveTree;
        private final TransactionTree transmitTree;

        public TransactionDescription(
                String messageName,
                TransactionTree receiveTree,
                TransactionTree transmitTree)
        {
            this.messageName = messageName;
            this.receiveTree = receiveTree;
            this.transmitTree = transmitTree;
        }

        public String getMessageName() {
            return messageName;
        }

        public TransactionTree getReceiveTree() {
            return receiveTree;
        }

        public TransactionTree getTransmitTree() {
            return transmitTree;
        }
    }
}
