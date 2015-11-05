package cocaine.msgpack;

import cocaine.message.Message;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

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
        packer.writeArrayBegin(3);
        packer.write(message.getSession());
        packer.write(message.getType());
        packer.write(message.getPayload());
        packer.writeArrayEnd();
    }

    @Override
    public Message read(Unpacker unpacker, Message message, boolean required) throws IOException {
        unpacker.readArrayBegin();
        long session = unpacker.readLong();
        int messageType = unpacker.readInt();
        Value payload = unpacker.readValue();
        unpacker.readArrayEnd();

        return new Message(messageType, session, payload);
    }

}
