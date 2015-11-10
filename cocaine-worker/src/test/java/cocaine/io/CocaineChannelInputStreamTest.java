package cocaine.io;

import org.junit.Assert;
import org.junit.Test;
import rx.Observable;

/**
 * @author akirakozov
 */
public class CocaineChannelInputStreamTest {

    @Test
    public void read() throws Exception {
        Observable<byte[]> observable = Observable.from("msg1".getBytes(), "msg2".getBytes(), "end".getBytes());
        CocaineChannelInputStream in = new CocaineChannelInputStream(observable);

        byte[] buffer = new byte[1000];
        int len = in.read(buffer);
        Assert.assertEquals("msg1msg2end", new String(buffer, 0, len));
    }

    @Test
    public void readPart() throws Exception {
        Observable<byte[]> observable = Observable.from("msg1".getBytes(), "next".getBytes(), "end".getBytes());
        CocaineChannelInputStream in = new CocaineChannelInputStream(observable);

        byte[] buffer = new byte[6];
        int len = in.read(buffer);
        Assert.assertEquals(buffer.length, len);
        Assert.assertEquals("msg1ne", new String(buffer));
    }
}
