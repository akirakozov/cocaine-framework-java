package cocaine.message;

import java.io.IOException;
import java.util.*;

import cocaine.hpack.HeaderField;
import cocaine.msgpack.MessageTemplate;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.msgpack.MessagePack;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class WorkerMessageTemplateTest {

    private MessagePack pack;

    private List<Object> rawHeaders;
    private List<HeaderField> headers;

    @Before
    public void setUp() {
        pack = new MessagePack();
        pack.register(UUID.class, UUIDTemplate.getInstance());
        pack.register(Message.class, new MessageTemplate());

        rawHeaders = new ArrayList<>();
        rawHeaders.add(Arrays.asList(true, 80, "traceeee"));
        rawHeaders.add(Arrays.asList(true, 81, "spannnnn"));
        rawHeaders.add(Arrays.asList(true, 82, "parenttt"));

        headers = new ArrayList<>();
        headers.add(new HeaderField("trace_id".getBytes(), "traceeee".getBytes()));
        headers.add(new HeaderField("span_id".getBytes(), "spannnnn".getBytes()));
        headers.add(new HeaderField("parent_id".getBytes(), "parenttt".getBytes()));
    }

    @Test
    public void writeHandshakeMessage() throws IOException {
        UUID uuid = UUID.randomUUID();
        WorkerMessage msg = Messages.handshake(uuid);
        byte[] bytes = pack.write(Arrays.asList(1, 0, Arrays.asList(uuid)));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeHeartbeatMessage() throws IOException {
        WorkerMessage msg = Messages.heartbeat();
        byte[] bytes = pack.write(Arrays.asList(1, 0, Arrays.asList()));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeTerminateMessage() throws IOException {
        String message = "PANIC!";
        WorkerMessage msg = Messages.terminate(TerminateMessage.Reason.NORMAL, message);
        byte[] bytes = pack.write(Arrays.asList(1, 1, Arrays.asList(1, message)));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeInvokeMessage() throws IOException {
        long session = 1L;
        String method = "invoke";

        WorkerMessage msg = Messages.invoke(session, headers, method);
        byte[] bytes = pack.write(Arrays.asList(session, 0, Arrays.asList(method), rawHeaders));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeWriteMessage() throws IOException {
        long session = 1L;
        byte[] data = new byte[] { 1, 2, 3, 4, 5 };

        WorkerMessage msg = Messages.write(session, data, headers);
        byte[] bytes = pack.write(Arrays.asList(session, 0, Collections.singletonList(data), rawHeaders));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeErrorMessage() throws IOException {
        long session = 1L;

        int category = 1;
        int code = -200;
        String message = "Failed!";

        WorkerMessage msg = Messages.error(session, headers, category, code, message);
        byte[] bytes = pack.write(Arrays.asList(session, 1, Arrays.asList(Arrays.asList(category, code), message), rawHeaders));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

    @Test
    public void writeCloseMessage() throws IOException {
        long session = 1L;

        WorkerMessage msg = Messages.close(session, headers);
        byte[] bytes = pack.write(Arrays.asList(session, 2, Arrays.asList(), rawHeaders));

        byte[] result = pack.write(msg, WorkerMessageTemplate.getInstance());

        Assert.assertArrayEquals(bytes, result);
    }

}
