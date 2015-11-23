package cocaine.http;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.util.*;

/**
 * @author akirakozov
 */
public class HttpInitialRequestTemplateTest {

    @Test
    public void read() throws Exception {
        MessagePack pack = new MessagePack();
        String method = "PUT";
        String path = "/get/info?id=123";
        String version = "1.1";
        List headers = Arrays.asList(
                Arrays.asList("header1", "value1"),
                Arrays.asList("header2", "value2"),
                Arrays.asList("header2", "value3"));
        byte[] data = "Binary data".getBytes();

        byte[] bytes = pack.write(Arrays.asList(method, path, version, headers, data));
        HttpInitialRequest request = pack.read(bytes, HttpInitialRequestTemplate.getInstance());

        Assert.assertEquals(method, request.getMethod());
        Assert.assertEquals(path, request.getPath());
        Assert.assertEquals(version, request.getHttpVersion());
        Assert.assertArrayEquals(data, request.getFirstBodyPart());
        for (int i = 1; i <= 2; i++) {
            Assert.assertEquals(Arrays.asList("value1"), request.getHeaders().get("header1"));
            Assert.assertEquals(Arrays.asList("value2", "value3"), request.getHeaders().get("header2"));
        }
    }
}