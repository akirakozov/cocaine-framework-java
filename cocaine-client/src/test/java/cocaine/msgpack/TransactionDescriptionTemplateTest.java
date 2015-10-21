package cocaine.msgpack;

import cocaine.api.ServiceApiV12.TransactionDescription;
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
public class TransactionDescriptionTemplateTest {

    @Test
    public void read() throws Exception {
        MessagePack pack = new MessagePack();
        // Logger transaction description example
        // ['verbosity', {}, {0: ['value', {}], 1: ['error', {}]}]
        //
        Map<Integer, Object> protocol = new HashMap<>();
        protocol.put(0, Arrays.asList("value", Collections.emptyMap()));
        protocol.put(1, Arrays.asList("error", Collections.emptyMap()));

        byte[] bytes = pack.write(Arrays.asList("verbosity", Collections.emptyMap(), protocol));

        TransactionDescription d = pack.read(bytes, TransactionDescriptionTemplate.getInstance());
        Assert.assertEquals("verbosity", d.getMessageName());
        Assert.assertEquals(0, d.getReceiveTree().getMessageId("value"));
        Assert.assertEquals(1, d.getReceiveTree().getMessageId("error"));

        Assert.assertTrue(d.getTransmitTree().isEmpty());
    }
}
