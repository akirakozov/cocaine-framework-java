package cocaine.api;

import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * @author akirakozov
 */
public class TransactionTreeTest {

    @Test
    public void getMessageId() throws Exception {
        TransactionTree tree = TransactionTree.SIMPLE_VALUE;
        Assert.assertEquals(tree.getMessageId("value"), 0);
        Assert.assertEquals(tree.getMessageId("error"), 1);
    }

    @Test
    public void getAllMessageTypesForSimpleValue() throws Exception {
        Set<String> expected = new HashSet<>(Arrays.asList("value", "error"));
        Assert.assertEquals(expected, TransactionTree.SIMPLE_VALUE.getAllMessageTypes());
    }

    @Test
    public void getAllMessageTypesForEmptyTree() throws Exception {
        Assert.assertEquals(Collections.emptySet(), TransactionTree.EMPTY.getAllMessageTypes());
    }

    @Test
    public void getAllMessageTypesForTreeWithSubtrees() throws Exception {
        Map<Integer, TransactionTree.TransactionInfo> map = new HashMap<>();
        map.put(0, new TransactionTree.TransactionInfo("value0", TransactionTree.EMPTY));
        map.put(1, new TransactionTree.TransactionInfo("value1", TransactionTree.SIMPLE_VALUE));
        map.put(2, new TransactionTree.TransactionInfo("value2", TransactionTree.SIMPLE_VALUE));

        TransactionTree tree = new TransactionTree(map);
        Set<String> expected = new HashSet<>(Arrays.asList("value", "error", "value0", "value1", "value2"));
        Assert.assertEquals(expected, tree.getAllMessageTypes());
    }

}
