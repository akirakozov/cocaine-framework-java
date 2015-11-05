package cocaine.msgpack;

import cocaine.api.ServiceApi.TransactionDescription;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.util.*;

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
        Assert.assertEquals(Optional.of(0), d.getReceiveTree().getMessageId("value"));
        Assert.assertEquals(Optional.of(1), d.getReceiveTree().getMessageId("error"));

        Assert.assertTrue(d.getTransmitTree().isEmpty());
    }
}
