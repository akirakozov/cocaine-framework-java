package cocaine.msgpack;

import cocaine.api.TransactionTree.TransactionInfo;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author akirakozov
 */
public class TransactionInfoTemplateTest {

    @Test
    public void read() throws IOException {
        MessagePack pack = new MessagePack();
        // ['value', {}]
        byte[] bytes = pack.write(Arrays.asList("value", Collections.emptyMap()));
        TransactionInfo info = pack.read(bytes, TransactionInfoTemplate.getInstance());

        Assert.assertEquals("value", info.getMessageName());
    }
}
