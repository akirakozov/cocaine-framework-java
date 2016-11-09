package cocaine.msgpack;

import cocaine.hpack.Decoder;
import cocaine.hpack.Encoder;
import cocaine.hpack.HeaderField;
import cocaine.service.InvocationUtils.InvocationRequest;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.List;


/**
 * @author antmat
 */
public class InvocationRequestTemplate extends AbstractTemplate<InvocationRequest> {

    private final Encoder encoder = new Encoder(Decoder.DEFAULT_TABLE_SIZE);

    @Override
    public void write(Packer packer, InvocationRequest invocationRequest, boolean required) throws IOException {
        List<HeaderField> headers = invocationRequest.headers;
        packer.writeArrayBegin(headers.isEmpty() ? 3 : 4);
        packer.write(invocationRequest.session);
        packer.write(invocationRequest.method);
        packer.write(invocationRequest.args);
        MsgPackUtils.packHeaders(packer, headers, encoder);
        packer.writeArrayEnd();
    }

    @Override
    public InvocationRequest read(Unpacker unpacker, InvocationRequest invocationRequest, boolean required) throws IOException {
        throw new UnsupportedOperationException("Reading InvocationRequest is not supported");
    }
}
