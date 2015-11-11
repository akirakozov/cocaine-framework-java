package cocaine.io;

import rx.Observer;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author akirakozov
 */
public class CocaineChannelOutputStream extends OutputStream {
    private final Observer<byte[]> observer;

    public CocaineChannelOutputStream(Observer<byte[]> observer) {
        this.observer = observer;
    }

    @Override
    public void write(int b) throws IOException {
        observer.onNext(new byte[]{(byte)b});
    }


    @Override
    public void write(byte b[]) throws IOException {
        observer.onNext(b);
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        observer.onNext(Arrays.copyOfRange(b, off, len));
    }

    @Override
    public void close() {
        observer.onCompleted();
    }
}
