package cocaine.msgpack;

import cocaine.messagev12.MessageV12;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.type.ValueFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

/**
 * @author akirakozov
 */
public class MessageV12TemplateTest {
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
        byte[] result = pack.write(new MessageV12(messageType, session, ValueFactory.createBooleanValue(true)), MessageV12Template.getInstance());
        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeReadMessageV12() throws IOException {
        MessageV12 message = new MessageV12(1, 0, ValueFactory.createBooleanValue(true));
        byte[] bytes = pack.write(message, MessageV12Template.getInstance());
        MessageV12 result = pack.read(bytes, MessageV12Template.getInstance());

        Assert.assertEquals(message, result);
    }

}
