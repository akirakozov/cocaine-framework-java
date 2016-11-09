package cocaine.msgpack;

import cocaine.hpack.HeaderField;
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

    private List<Object> rawHeaders;
    private List<HeaderField> headers;

    @Before
    public void setUp() {
        pack = new MessagePack();

        rawHeaders = new ArrayList<>();
        rawHeaders.add(Arrays.asList(true, 80, "traceeee"));
        rawHeaders.add(Arrays.asList(true, 81, "spannnnn"));
        rawHeaders.add(Arrays.asList(true, 82, new byte[8]));

        headers = new ArrayList<>();
        headers.add(new HeaderField("trace_id".getBytes(), "traceeee".getBytes()));
        headers.add(new HeaderField("span_id".getBytes(), "spannnnn".getBytes()));
        headers.add(new HeaderField("parent_id".getBytes(), new byte[8]));
    }

    @Test
    public void writeMessageV12() throws IOException {
        long session = 1;
        int messageType = 0;

        MessageTemplate template = new MessageTemplate();
        byte[] bytes = pack.write(Arrays.asList(session, messageType, true, rawHeaders));

        Message m = new Message(messageType, session, ValueFactory.createBooleanValue(true), headers);
        byte[] result = pack.write(m, template);

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeReadMessageV12() throws IOException {
        MessageTemplate template = new MessageTemplate();

        byte[] bytes = pack.write(Arrays.asList(1, 0, true, rawHeaders));
        Message result = pack.read(bytes, template);

        byte[] bytes2 = pack.write(result, template);
        Message result2 = pack.read(bytes2, template);

        Assert.assertEquals(result, result2);

        Message message = new Message(0, 1, ValueFactory.createBooleanValue(true), headers);
        Assert.assertEquals(result, message);
    }
}
