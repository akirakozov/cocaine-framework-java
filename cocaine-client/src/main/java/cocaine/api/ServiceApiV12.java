package cocaine.api;

import java.util.Map;

/**
 * @author akirakozov
 */
public class ServiceApiV12 {
    private final Map<Integer, TransactionDescription> transactions;

    public ServiceApiV12(Map<Integer, TransactionDescription> transactions) {
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
                .findFirst().get().getKey();
    }

    private TransactionDescription findDescription(String name) {
        return transactions.values().stream()
                .filter(info -> info.getMessageName().equals(name)).findFirst().get();
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
