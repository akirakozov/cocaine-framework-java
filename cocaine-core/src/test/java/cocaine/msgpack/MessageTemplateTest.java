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
        headers.add(Arrays.asList(false, "X-Request-Id", "tracerid"));
        headers.add(Arrays.asList(false, 81, "spanrid"));
        headers.add(Arrays.asList(false, 82, new byte[]{100, 101, -1, 99, 102, 103, -2, 104}));

        byte[] bytes = pack.write(Arrays.asList(session, messageType, true, headers));
        byte[] result = pack.write(new Message(messageType, session, ValueFactory.createBooleanValue(true), headers),
                MessageTemplate.getInstance());
        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeReadMessageV12() throws IOException {
        List<Object> headers = new ArrayList<>();
        headers.add(80);
        headers.add(Arrays.asList(false, 85, "spanrid"));
        headers.add(Arrays.asList(false, 82, new byte[]{100, 101, -1, 99, 102, 103, -2, 104}));

        byte[] bytes = pack.write(Arrays.asList(1, 0, true, headers));
        Message result = pack.read(bytes, MessageTemplate.getInstance());

        byte[] bytes2 = pack.write(result, MessageTemplate.getInstance());
        Message result2 = pack.read(bytes2, MessageTemplate.getInstance());

        Assert.assertEquals(result, result2);

        Message message = new Message(0, 1, ValueFactory.createBooleanValue(true), new ArrayList<>());
        Assert.assertEquals(result, message);
    }

}
