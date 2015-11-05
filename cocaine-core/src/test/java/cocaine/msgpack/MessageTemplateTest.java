package cocaine.msgpack;

import cocaine.message.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.type.ValueFactory;

import java.io.IOException;
import java.util.Arrays;

/**
 * @author akirakozov
 */
public class MessageTemplateTest {
    private MessagePack pack;

    @Before
    public void setUp() {
        this.pack = new MessagePack();
    }

    @Test
    public void writeMessageV12() throws IOException {
        long session = 1;
        int messageType = 0;
        byte[] bytes = pack.write(Arrays.asList(session, messageType, true));
        byte[] result = pack.write(new Message(messageType, session, ValueFactory.createBooleanValue(true)), MessageTemplate.getInstance());
        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeReadMessageV12() throws IOException {
        Message message = new Message(1, 0, ValueFactory.createBooleanValue(true));
        byte[] bytes = pack.write(message, MessageTemplate.getInstance());
        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(message, result);
    }

}
