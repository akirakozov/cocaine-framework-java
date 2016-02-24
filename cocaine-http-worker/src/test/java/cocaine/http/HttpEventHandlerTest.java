package cocaine.http;


import org.apache.log4j.BasicConfigurator;
import org.junit.Assert;
import org.junit.Test;
import org.msgpack.MessagePack;
import rx.Observable;
import rx.observers.TestObserver;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author akirakozov
 */
public class HttpEventHandlerTest {
    private static final MessagePack PACK = new MessagePack();

    private static class HelloServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
            resp.getOutputStream().write("Hello!".getBytes());
        }
    }

    @Test
    public void simpleGetHandler() throws Exception {
        BasicConfigurator.configure();

        HttpEventHandler handler = new HttpEventHandler(new HelloServlet());
        Observable<byte[]> request = Observable.from(createHttpEvent());
        TestObserver<byte[]> response = new TestObserver<>();

        handler.handle(request, response);
        List<byte[]> results = response.getOnNextEvents();
        Assert.assertEquals("Incorrect events count", 2, results.size());
        Assert.assertEquals("Incoreect status code", 200, getStatusCode(results.get(0)));
        Assert.assertEquals("Incorrect respone", "Hello!", new String(results.get(1)));
    }

    private int getStatusCode(byte[] data) throws IOException {
        return PACK.read(data).asArrayValue().get(0).asIntegerValue().getInt();
    }

    private byte[] createHttpEvent() throws IOException {
        String method = "GET";
        String path = "/hello";
        String version = "1.1";
        byte[] body = new byte[0];

        return PACK.write(Arrays.asList(method, path, version, Collections.emptyList(), body));
    }
}
