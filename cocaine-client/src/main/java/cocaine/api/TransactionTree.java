package cocaine.api;

import java.util.*;

/**
 * @author akirakozov
 */
public class TransactionTree {
    private final Map<Integer, TransactionInfo> transactions;
    public static TransactionTree EMPTY = new TransactionTree(Collections.emptyMap());
    public static TransactionTree CYCLE = new CycleTree();
    public static TransactionTree SIMPLE_VALUE = createSimpleValueTree();
    public static TransactionTree STREAMING = createStreamingTree();


    public TransactionTree(Map<Integer, TransactionInfo> transactions) {
        this.transactions = transactions;
    }

    public Optional<TransactionInfo> getInfo(int messageId) {
        return Optional.ofNullable(transactions.get(messageId));
    }

    public Optional<TransactionInfo> getInfo(String name) {
        return transactions.values().stream()
                .filter(info -> info.messageName.equals(name))
                .findFirst();
    }

    public Optional<Integer> getMessageId(String name) {
        return transactions.entrySet().stream()
                .filter(it -> it.getValue().messageName.equals(name))
                .findFirst().map(Map.Entry::getKey);
    }

    public boolean isEmpty() {
        if (isCycle()) {
            return false;
        }
        return transactions.isEmpty();
    }

    public Set<String> getAllMessageTypes() {
        HashSet<String> values = new HashSet<>();
        for (TransactionInfo info : transactions.values()) {
            values.add(info.getMessageName());
            values.addAll(info.getTree().getAllMessageTypes());
        }
        return values;
    }

    public boolean isCycle() {
        return false;
    }

    private static TransactionTree createStreamingTree() {
        // {0: ['write', None], 1: ['error', {}], 2: ['close', {}]}
        Map<Integer, TransactionInfo> tree = new HashMap<>();
        tree.put(0, new TransactionInfo("write", CYCLE));
        tree.put(1, new TransactionInfo("error", EMPTY));
        tree.put(2, new TransactionInfo("close", EMPTY));
        return new TransactionTree(tree);
    }

    private static TransactionTree createSimpleValueTree() {
        // {0: ['value', {}], 1: ['error', {}]}
        Map<Integer, TransactionInfo> tree = new HashMap<>();
        tree.put(0, new TransactionInfo("value", EMPTY));
        tree.put(1, new TransactionInfo("error", EMPTY));
        return new TransactionTree(tree);
    }

    private static class CycleTree extends TransactionTree {
        private CycleTree() {
            super(Collections.emptyMap());
        }

        public boolean isCycle() {
            return true;
        }
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
