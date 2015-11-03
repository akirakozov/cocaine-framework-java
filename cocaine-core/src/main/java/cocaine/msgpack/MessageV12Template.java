package cocaine.msgpack;

import cocaine.messagev12.MessageV12;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * @author akirakozov
 */
public final class MessageV12Template<T> extends AbstractTemplate<MessageV12> {

    private static final Template<MessageV12> instance = new MessageV12Template();
    private MessageV12Template() {
    }

    public static Template<MessageV12> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, MessageV12 message, boolean required) throws IOException {
        packer.writeArrayBegin(3);
        packer.write(message.getSession());
        packer.write(message.getType());
        packer.write(message.getPayload());
        packer.writeArrayEnd();
    }

    @Override
    public MessageV12 read(Unpacker unpacker, MessageV12 message, boolean required) throws IOException {
        unpacker.readArrayBegin();
        long session = unpacker.readLong();
        int messageType = unpacker.readInt();
        Value payload = unpacker.readValue();
        unpacker.readArrayEnd();

        return new MessageV12(messageType, session, payload);
    }

}
