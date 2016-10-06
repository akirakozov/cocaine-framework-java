package cocaine.msgpack;

import cocaine.message.Message;
import cocaine.request.RequestIdStack;
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
                Value headerValue = unpacker.readValue();
                if (headerValue.getType() != ValueType.INTEGER) {
                    List<Object> header = new ArrayList<>();
                    ArrayValue array = headerValue.asArrayValue();
                    for (Value part : array) {
                        header.add(readHeaderPart(part));
                    }
                    headers.add(header);
                }
            }
            if (headers.size() != RequestIdStack.AVAILABLE_IDS.size()) {
                headers.clear();
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
                return part.asRawValue().getString();
            default:
                return part.toString();
        }
    }

}
