package cocaine.http;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * @author akirakozov
 */
public final class HttpInitialRequestTemplate extends AbstractTemplate<HttpInitialRequest> {

    private static final Template<HttpInitialRequest> instance = new HttpInitialRequestTemplate();

    private HttpInitialRequestTemplate() {}

    public static Template<HttpInitialRequest> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer pk, HttpInitialRequest v, boolean required) throws IOException {
        throw new UnsupportedOperationException("Unsupported");
    }

    @Override
    public HttpInitialRequest read(Unpacker unpacker, HttpInitialRequest to, boolean required) throws IOException {
        unpacker.readArrayBegin();
        String method = unpacker.readString();
        String path = unpacker.readString();
        String version = unpacker.readString();
        // headers
        int headersCount = unpacker.readArrayBegin();
        Multimap<String, String> headers = ArrayListMultimap.create();
        for (int i = 0; i < headersCount; i++) {
            unpacker.readArrayBegin();
            String header = unpacker.readString();
            String value = unpacker.readString();
            headers.put(header, value);
            unpacker.readArrayEnd();
        }
        unpacker.readArrayEnd();
        // body first part
        byte[] data = unpacker.readByteArray();
        unpacker.readArrayEnd();

        return new HttpInitialRequest(method, path, version, headers, data);
    }
}
