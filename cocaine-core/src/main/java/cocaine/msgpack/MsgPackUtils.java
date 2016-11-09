package cocaine.msgpack;

import cocaine.hpack.Encoder;
import cocaine.hpack.HeaderField;
import org.msgpack.packer.Packer;

import java.io.IOException;
import java.util.List;

/**
 * @author metal
 */
public class MsgPackUtils {
    public static void packHeaders(Packer packer, List<HeaderField> headers, Encoder encoder) throws IOException {
        if (!headers.isEmpty()) {
            packer.writeArrayBegin(headers.size());
            for (HeaderField h : headers) {
                encoder.encodeHeader(packer, h.name, h.value, true);
            }
            packer.writeArrayEnd();
        }
    }
}
