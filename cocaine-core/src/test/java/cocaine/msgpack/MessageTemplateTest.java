package cocaine.msgpack;

import cocaine.message.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.type.ValueFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        List<List<Object>> headers = new ArrayList<>();
        headers.add(Arrays.asList(80));
        headers.add(Arrays.asList(false, 81, "reqreqid"));
        headers.add(Arrays.asList(false, "X-Request-Id", "tracetid"));

        byte[] bytes = pack.write(Arrays.asList(session, messageType, true, headers));
        byte[] result = pack.write(new Message(messageType, session, ValueFactory.createBooleanValue(true), headers),
                MessageTemplate.getInstance());
        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeReadMessageV12() throws IOException {
        List<List<Object>> headers = new ArrayList<>();
        headers.add(Arrays.asList(80));
        headers.add(Arrays.asList(false, 81, "reqreqid"));
        headers.add(Arrays.asList(false, "X-Request-Id", "tracetid"));

        Message message = new Message(1, 0, ValueFactory.createBooleanValue(true), headers);
        byte[] bytes = pack.write(message, MessageTemplate.getInstance());
        Message result = pack.read(bytes, MessageTemplate.getInstance());

        Assert.assertEquals(message, result);
    }

}
