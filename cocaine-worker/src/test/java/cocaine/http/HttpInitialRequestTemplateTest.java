package cocaine.http;

import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

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
        Map<String, String> map = new HashMap<>();
        map.put("header1", "value1");
        map.put("header2", "value2");
        byte[] data = "Binary data".getBytes();

        byte[] bytes = pack.write(Arrays.asList(method, path, version, map, data));
        HttpInitialRequest request = pack.read(bytes, HttpInitialRequestTemplate.getInstance());

        Assert.assertEquals(method, request.getMethod());
        Assert.assertEquals(path, request.getPath());
        Assert.assertEquals(version, request.getHttpVersion());
        Assert.assertEquals(map, request.getHeaders());
        Assert.assertArrayEquals(data, request.getFirstBodyPart());
    }
}