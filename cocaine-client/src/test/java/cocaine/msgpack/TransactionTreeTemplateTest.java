package cocaine.msgpack;

import cocaine.api.TransactionTree;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class TransactionTreeTemplateTest {

    @Test
    public void read() throws Exception {
        MessagePack pack = new MessagePack();
        // Logger transaction tree
        //{
        //    0: ['emit', {}, {}],
        //    1: ['verbosity', {}, {0: ['value', {}], 1: ['error', {}]}],
        //    2: ['set_verbosity', {}, {0: ['value', {}], 1: ['error', {}]}]
        //}
        Map<Integer, Object> protocol = new HashMap<>();
        protocol.put(0, Arrays.asList("values", Collections.emptyMap()));
        protocol.put(1, Arrays.asList("error", Collections.emptyMap()));

        Map<Integer, Object> tree = new HashMap<>();
        tree.put(0, Arrays.asList("emit", Collections.emptyMap(), Collections.emptyMap()));
        tree.put(1, Arrays.asList("verbosity", Collections.emptyMap(), protocol));
        tree.put(2, Arrays.asList("set_verbosity", Collections.emptyMap(), protocol));

        byte[] bytes = pack.write(tree);
        TransactionTree trTree = pack.read(bytes, TransactionTreeTemplate.getInstance());
        TransactionTree.TransactionInfo info = trTree.getInfo(0);

        Assert.assertNotNull(info);
        Assert.assertEquals("emit", info.getMessageName());
        Assert.assertEquals(0, trTree.getMessageId("emit"));
        Assert.assertEquals(1, trTree.getMessageId("verbosity"));
        Assert.assertEquals(2, trTree.getMessageId("set_verbosity"));
    }
}
