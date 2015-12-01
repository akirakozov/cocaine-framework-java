package cocaine.http.io;

import cocaine.http.HttpCocaineResponse;
import cocaine.io.CocaineChannelOutputStream;
import org.msgpack.MessagePack;
import rx.Observer;

import javax.servlet.ServletOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author akirakozov
 */
public class HttpCocaineOutputStream extends ServletOutputStream {
    private static final MessagePack PACK = new MessagePack();

    private final Lock metaDataWriteLock = new ReentrantLock();
    private volatile boolean isMetaDataCommitted = false;
    private final OutputStream outputStream;
    private final HttpCocaineResponse response;

    public HttpCocaineOutputStream(Observer<byte[]> output, HttpCocaineResponse response) {
        this.outputStream = new CocaineChannelOutputStream(output);
        this.response = response;
    }

    @Override
    public void write(int b) throws IOException {
        writeMetaDataIfNotYet();
        outputStream.write(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        writeMetaDataIfNotYet();
        outputStream.write(b, off, len);
    }

    @Override
    public void close() throws IOException {
        writeMetaDataIfNotYet();
        outputStream.close();
    }

    private void writeMetaDataIfNotYet() throws IOException {
        if (!isMetaDataCommitted) {
            metaDataWriteLock.lock();
            try {
                if (!isMetaDataCommitted) {
                    outputStream.write(serializeMetaData());
                    isMetaDataCommitted = true;
                }
            } finally {
                metaDataWriteLock.unlock();
            }
        }
    }

    private byte[] serializeMetaData() throws IOException {
        int status = response.getStatus();
        // TODO: serialize HEADERS and other meta information
        return PACK.write(Arrays.asList(status, Arrays.asList()));
    }
}
