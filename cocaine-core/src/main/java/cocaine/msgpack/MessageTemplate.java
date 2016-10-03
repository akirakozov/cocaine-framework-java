package cocaine.msgpack;

import cocaine.message.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
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

                int headerLength = unpacker.readArrayBegin();
                for (int j = 0; j < headerLength; j++) {
                    header.add(readHeaderPart(unpacker));
                }
                unpacker.readArrayEnd();

                headers.add(header);
            }

            unpacker.readArrayEnd();
        }

        unpacker.readArrayEnd();

        return new Message(messageType, session, payload, headers);
    }

    private Object readHeaderPart(Unpacker unpacker) throws IOException {
        Value value = unpacker.readValue();
        switch (value.getType()) {
            case BOOLEAN:
                return value.asBooleanValue().getBoolean();
            case INTEGER:
                return value.asIntegerValue().getInt();
            case RAW:
                return value.asRawValue().toString();
            default:
                return value.toString();
        }
    }

}
