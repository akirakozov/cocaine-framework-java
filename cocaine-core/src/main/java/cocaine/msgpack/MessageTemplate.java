package cocaine.msgpack;

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
import java.util.List;

/**
 * @author akirakozov
 */
public final class MessageTemplate<T> extends AbstractTemplate<Message> {
    private static final Logger logger = Logger.getLogger(MessageTemplate.class);

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
            try {
                for (int i = 0; i < count; i++) {
                    Value headerValue = unpacker.readValue();
                    if (headerValue.getType() == ValueType.ARRAY) {
                        ArrayValue array = headerValue.asArrayValue();
                        List<Object> header = parseHeader(array);
                        if (!header.isEmpty()) {
                            headers.add(header);
                        }
                    }
                }

            } catch (MessageTypeException e) {
                logger.warn("Reading message failed; session " + session + ", messageType " + messageType, e);
            } finally {
                unpacker.readArrayEnd();
            }
        }

        unpacker.readArrayEnd();

        if (headers.size() != RequestIdStack.AVAILABLE_IDS.size()) {
            headers.clear();
        }
        return new Message(messageType, session, payload, headers);
    }

    private Object readHeaderPart(Value part) {
        switch (part.getType()) {
            case BOOLEAN:
                return part.asBooleanValue().getBoolean();
            case INTEGER:
                return part.asIntegerValue().getInt();
            case RAW:
                return part.asRawValue().getByteArray();
            default:
                throw new MessageTypeException("Failed to parse header part");
        }
    }

    private List<Object> parseHeader(ArrayValue array) {
        List<Object> result = new ArrayList<>();

        if (array.size() == 3
                && array.get(0).getType() == ValueType.BOOLEAN
                && array.get(1).getType() == ValueType.INTEGER
                && array.get(2).getType() == ValueType.RAW)
        {
            Integer headerIndex = (Integer) readHeaderPart(array.get(1));
            if (RequestIdStack.Type.byHeaderIndex(headerIndex) != null) {
                result.add(readHeaderPart(array.get(0)));
                result.add(headerIndex);
                result.add(readHeaderPart(array.get(2)));
            }
        }

        return result;
    }

}
