package cocaine.msgpack;

import cocaine.hpack.Decoder;
import cocaine.hpack.Encoder;
import cocaine.hpack.HeaderField;
import cocaine.message.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


/**
 * @author akirakozov
 */
public final class MessageTemplate extends AbstractTemplate<Message> {
    private final Decoder decoder = new Decoder(Decoder.DEFAULT_TABLE_SIZE);
    private final Encoder encoder = new Encoder(Decoder.DEFAULT_TABLE_SIZE);

    @Override
    public void write(Packer packer, Message message, boolean required) throws IOException {
        List<HeaderField> headers = message.getHeaders();

        packer.writeArrayBegin(headers.isEmpty() ? 3 : 4);
        packer.write(message.getSession());
        packer.write(message.getType());
        packer.write(message.getPayload());

        MsgPackUtils.packHeaders(packer, headers, encoder);
        packer.writeArrayEnd();
    }

    @Override
    public Message read(Unpacker unpacker, Message message, boolean required) throws IOException {
        int size = unpacker.readArrayBegin();
        long session = unpacker.readLong();
        int messageType = unpacker.readInt();
        Value payload = unpacker.readValue();

        List<HeaderField> headers;
        if (size == 4) {
            Value rawHeaders = unpacker.readValue();
            headers = decoder.decode(rawHeaders);
        } else {
            headers = new ArrayList<>();
        }

        unpacker.readArrayEnd();

        return new Message(messageType, session, payload, headers);
    }
}
