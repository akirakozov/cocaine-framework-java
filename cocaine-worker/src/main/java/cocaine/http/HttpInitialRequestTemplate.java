package cocaine.http;

import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.template.Templates;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.Map;

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
        Map<String, String> headers = unpacker.read(Templates.tMap(Templates.TString, Templates.TString));
        byte[] data = unpacker.readByteArray();
        unpacker.readArrayEnd();

        return new HttpInitialRequest(method, path, version, headers, data);
    }
}
