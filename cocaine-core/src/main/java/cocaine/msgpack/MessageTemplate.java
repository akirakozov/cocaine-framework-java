package cocaine.msgpack;

import cocaine.hpack.Decoder;
import cocaine.hpack.Encoder;
import cocaine.hpack.HeaderField;
import cocaine.message.Message;
import cocaine.request.RequestIdStack;
import org.apache.log4j.Logger;
import org.msgpack.MessageTypeException;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;


/**
 * @author akirakozov
 */
public final class
MessageTemplate extends AbstractTemplate<Message> {
    private static final Logger logger = Logger.getLogger(MessageTemplate.class);

    private final Decoder decoder;
    private final Encoder encoder;

    public MessageTemplate() {
        this.decoder = new Decoder(Decoder.DEFAULT_TABLE_SIZE);
        this.encoder = new Encoder(Decoder.DEFAULT_TABLE_SIZE);
    }

    @Override
    public void write(Packer packer, Message message, boolean required) throws IOException {
        if(message.getHeaders() == null) {
            return;
        }
        packer.writeArrayBegin(message.getHeaders().isEmpty() ? 3 : 4);
        packer.write(message.getSession());
        packer.write(message.getType());
        packer.write(message.getPayload());
        List<HeaderField> headers = message.getHeaders();
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
    public Message read(Unpacker unpacker, Message message, boolean required) throws IOException {
        int size = unpacker.readArrayBegin();
        long session = unpacker.readLong();
        int messageType = unpacker.readInt();
        Value payload = unpacker.readValue();

        Value raw_headers = null;
        List<HeaderField> headers;

        if (size == 4) {
            raw_headers = unpacker.readValue();
            headers = decoder.decode(raw_headers);
        } else {
            headers = new ArrayList<>();
        }

        return new Message(messageType, session, payload, headers);
    }


}
