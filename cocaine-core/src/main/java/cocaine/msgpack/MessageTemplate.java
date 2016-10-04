package cocaine.msgpack;

import cocaine.message.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author akirakozov
 */
public final class MessageTemplate<T> extends AbstractTemplate<Message> {

    private static final Template<Message> instance = new MessageTemplate();
    private MessageTemplate() {
    }

    public static Template<Message> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, Message message, boolean required) throws IOException {
        packer.writeArrayBegin(message.getHeaders().isEmpty() ? 3 : 4);
        packer.write(message.getSession());
        packer.write(message.getType());
        packer.write(message.getPayload());
        if (!message.getHeaders().isEmpty()) {
            packer.write(message.getHeaders());
        }
        packer.writeArrayEnd();
    }

    @Override
    public Message read(Unpacker unpacker, Message message, boolean required) throws IOException {
        int size = unpacker.readArrayBegin();
        long session = unpacker.readLong();
        int messageType = unpacker.readInt();
        Value payload = unpacker.readValue();

        List<List<Object>> headers = new ArrayList<>();
        if (size == 4) {
            int count = unpacker.readArrayBegin();

            for (int i = 0; i < count; i++) {
                List<Object> header = new ArrayList<>();

                Value headerValue = unpacker.readValue();
                if (headerValue.getType() == ValueType.INTEGER) {
                    header.add(headerValue.asIntegerValue().getInt());
                } else {
                    ArrayValue array = headerValue.asArrayValue();
                    for (Value part : array) {
                        header.add(readHeaderPart(part));
                    }
                }

                headers.add(header);
            }

            unpacker.readArrayEnd();
        }

        unpacker.readArrayEnd();

        return new Message(messageType, session, payload, headers);
    }

    private Object readHeaderPart(Value part) throws IOException {
        switch (part.getType()) {
            case BOOLEAN:
                return part.asBooleanValue().getBoolean();
            case INTEGER:
                return part.asIntegerValue().getInt();
            case RAW:
                return part.asRawValue().toString();
            default:
                return part.toString();
        }
    }

}
