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
        TransactionTree.TransactionInfo info = trTree.getInfo(0).get();

        Assert.assertNotNull(info);
        Assert.assertEquals("value", info.getMessageName());
        Assert.assertEquals(0, trTree.getMessageId("value"));
        Assert.assertEquals(1, trTree.getMessageId("error"));
    }

    @Test
    public void readSteamProtocol() throws Exception {
        MessagePack pack = new MessagePack();
        // {0: ['write', None], 1: ['error', {}], 2: ['close', {}]}
        Map<Integer, Object> protocol = new HashMap<>();
        protocol.put(0, Arrays.asList("write", null));
        protocol.put(1, Arrays.asList("error", Collections.emptyMap()));
        protocol.put(2, Arrays.asList("close", Collections.emptyMap()));

        byte[] bytes = pack.write(protocol);
        TransactionTree trTree = pack.read(bytes, TransactionTreeTemplate.getInstance());
        TransactionTree.TransactionInfo info = trTree.getInfo(0).get();

        Assert.assertNotNull(info);
        Assert.assertEquals("write", info.getMessageName());
        Assert.assertTrue(info.getTree().isCycle());
        Assert.assertEquals(0, trTree.getMessageId("write"));
        Assert.assertEquals(1, trTree.getMessageId("error"));
    }
}
