package cocaine.msgpack;

import cocaine.hpack.Decoder;
import cocaine.hpack.Encoder;
import cocaine.hpack.HeaderField;
import cocaine.service.InvocationUtils;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.List;


/**
 * @author antmat
 */
public class InvocationRequestTemplate extends AbstractTemplate<InvocationUtils.InvocationRequest> {

    private final Encoder encoder = new Encoder(Decoder.DEFAULT_TABLE_SIZE);

    @Override
    public void write(Packer packer, InvocationUtils.InvocationRequest invocationRequest, boolean b) throws IOException {
        List<HeaderField> headers = invocationRequest.headers;
        packer.writeArrayBegin(headers.isEmpty() ? 3 : 4);
        packer.write(invocationRequest.session);
        packer.write(invocationRequest.method);
        packer.write(invocationRequest.args);
        if (!headers.isEmpty()) {
            packer.writeArrayBegin(headers.size());
            for(int i = 0; i < headers.size(); i++) {
                HeaderField h = headers.get(i);
                encoder.encodeHeader(packer, h.name, h.value, true);
            }
            packer.writeArrayEnd();
        }
        packer.writeArrayEnd();
    }

    @Override
    public InvocationUtils.InvocationRequest read(Unpacker unpacker, InvocationUtils.InvocationRequest invocationRequest, boolean b) throws IOException {
        throw new UnsupportedOperationException("Reading InvocationRequest is not supported");
    }
}
