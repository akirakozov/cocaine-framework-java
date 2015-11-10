package cocaine.io;

import rx.Observable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * @author akirakozov
 */
public class CocaineChannelInputStream extends InputStream {
    private Iterator<byte[]> it;
    private int offset = 0;
    private byte[] buffer;

    public CocaineChannelInputStream(Observable<byte[]> observable) {
        it = observable.toBlocking().getIterator();
        offset = 0;
    }


    @Override
    public int read() throws IOException {
        if (buffer == null) {
            if (it.hasNext()) {
                buffer = it.next();
            } else {
                return -1;
            }
        }

        if (offset == buffer.length && it.hasNext()) {
            buffer = it.next();
            offset = 0;
        }

        if (offset < buffer.length) {
            return buffer[offset++];
        }

        return -1;
    }
}
