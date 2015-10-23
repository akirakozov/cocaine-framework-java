package cocaine.api;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class TransactionTree {
    private final Map<Integer, TransactionInfo> transactions;
    public static TransactionTree EMPTY = new TransactionTree(Collections.emptyMap());
    public static TransactionTree SIMPLE_VALUE = createSimpleValueTree();

    public TransactionTree(Map<Integer, TransactionInfo> transactions) {
        this.transactions = transactions;
    }

    public TransactionInfo getInfo(int messageId) {
        return transactions.get(messageId);
    }

    public TransactionInfo getInfo(String name) {
        return transactions.values().stream()
                .filter(info -> info.messageName.equals(name)).findFirst().get();
    }

    public int getMessageId(String name) {
        return transactions.entrySet().stream()
                .filter(it -> it.getValue().messageName.equals(name))
                .findFirst().get().getKey();
    }

    public boolean isEmpty() {
        return transactions.isEmpty();
    }

    private static TransactionTree createSimpleValueTree() {
        // {0: ['value', {}], 1: ['error', {}]}
        Map<Integer, TransactionInfo> tree = new HashMap<>();
        tree.put(0, new TransactionInfo("value", EMPTY));
        tree.put(1, new TransactionInfo("error", EMPTY));
        return new TransactionTree(tree);
    }

    public static class TransactionInfo {
        private final String messageName;
        private final TransactionTree tree;

        public TransactionInfo(String messageName, TransactionTree tree) {
            this.messageName = messageName;
            this.tree = tree;
        }

        public TransactionTree getTree() {
            return tree;
        }

        public String getMessageName() {
            return messageName;
        }

    }
}
