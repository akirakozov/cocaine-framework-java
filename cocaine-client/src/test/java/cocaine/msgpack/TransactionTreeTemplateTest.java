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
        // {0: ['value', {}], 1: ['error', {}]}
        Map<Integer, Object> protocol = new HashMap<>();
        protocol.put(0, Arrays.asList("value", Collections.emptyMap()));
        protocol.put(1, Arrays.asList("error", Collections.emptyMap()));

        byte[] bytes = pack.write(protocol);
        TransactionTree trTree = pack.read(bytes, TransactionTreeTemplate.getInstance());
        TransactionTree.TransactionInfo info = trTree.getInfo(0);

        Assert.assertNotNull(info);
        Assert.assertEquals("value", info.getMessageName());
        Assert.assertEquals(0, trTree.getMessageId("value"));
        Assert.assertEquals(1, trTree.getMessageId("error"));
    }
}
