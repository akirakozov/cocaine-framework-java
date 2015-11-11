package cocaine.io;

import org.junit.Assert;
import org.junit.Test;
import rx.Observer;
import rx.exceptions.Exceptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class CocaineChannelOutputStreamTest {

    private static class Writer implements Observer<byte[]> {
        private final ByteArrayOutputStream out = new ByteArrayOutputStream();

        @Override
        public void onCompleted() {
            try {
                out.close();
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        }

        @Override
        public void onError(Throwable e) {
            throw Exceptions.propagate(e);
        }

        @Override
        public void onNext(byte[] bytes) {
            try {
                out.write(bytes);
            } catch (IOException e) {
                throw Exceptions.propagate(e);
            }
        }

        byte[] getBytes() {
            return out.toByteArray();
        }
    }

    @Test
    public void write() throws Exception {
        Writer writer = new Writer();
        CocaineChannelOutputStream out = new CocaineChannelOutputStream(writer);

        out.write(new String("msg1").getBytes());
        out.write(new String("msg2").getBytes());
        out.write(new String("ms").getBytes(), 0, 2);
        out.close();

        Assert.assertEquals("msg1msg2ms", new String(writer.getBytes()));
    }
}
