package cocaine.http.io;

import javax.servlet.ServletInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author akirakozov
 */
public class HttpCocaineInputStream extends ServletInputStream {
    private final InputStream input;

    public HttpCocaineInputStream(InputStream input) {
        this.input = input;
    }

    @Override
    public int read() throws IOException {
        return input.read();
    }
}
