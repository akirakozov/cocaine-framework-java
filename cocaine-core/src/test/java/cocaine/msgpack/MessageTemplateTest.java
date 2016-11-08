package cocaine.msgpack;

import cocaine.hpack.Decoder;
import cocaine.hpack.Encoder;
import cocaine.hpack.HeaderField;
import cocaine.message.Message;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;
import org.msgpack.type.Value;
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
        List<Object> raw_headers = new ArrayList<>();
        raw_headers.add(Arrays.asList(true, 80, "qwerasdf"));
        raw_headers.add(Arrays.asList(true, "span_id", "spanride"));
        raw_headers.add(Arrays.asList(true, 82, "spanride"));

        List<HeaderField> headers = new ArrayList<>();
        headers.add(new HeaderField("trace_id".getBytes(), "qwertasdf".getBytes()));
        headers.add(new HeaderField("span_id".getBytes(), "qwertasdf".getBytes()));
        headers.add(new HeaderField("parent_id".getBytes(), new byte[8]));

        MessageTemplate template = new MessageTemplate();
        byte[] bytes = pack.write(Arrays.asList(session, messageType, true, raw_headers));

        Message m = new Message(messageType, session, ValueFactory.createBooleanValue(true), headers);
        byte[] result = pack.write(m, template);
        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeReadMessageV12() throws IOException {
        List<Object> raw_headers = new ArrayList<>();
        raw_headers.add(Arrays.asList(true, 80, "qwerasdf"));
        raw_headers.add(Arrays.asList(true, "span_id", "spanride"));
        raw_headers.add(Arrays.asList(true, 82, "spanride"));

        List<HeaderField> headers = new ArrayList<>();
        headers.add(new HeaderField("trace_id".getBytes(), "qwerasdf".getBytes()));
        headers.add(new HeaderField("span_id".getBytes(), "spanride".getBytes()));
        headers.add(new HeaderField("parent_id".getBytes(), "spanride".getBytes()));

        Decoder decoder =  new Decoder(Decoder.DEFAULT_TABLE_SIZE);
        Encoder encoder =  new Encoder(Decoder.DEFAULT_TABLE_SIZE);
        MessageTemplate template = new MessageTemplate();

        byte[] bytes = pack.write(Arrays.asList(1, 0, true, raw_headers));
        Message result = pack.read(bytes, template);

        byte[] bytes2 = pack.write(result, template);
        Message result2 = pack.read(bytes2, template);

        Assert.assertEquals(result, result2);

        Message message = new Message(0, 1, ValueFactory.createBooleanValue(true), headers);
        Assert.assertEquals(result, message);
    }

}
