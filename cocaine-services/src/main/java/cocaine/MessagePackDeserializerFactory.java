package cocaine;

import cocaine.session.CocainePayloadDeserializer;
import cocaine.session.MessagePackPayloadDeserializer;
import org.msgpack.MessagePack;

/**
 * @author akirakozov
 */
public class MessagePackDeserializerFactory implements CocainePayloadDeserializerFactory {
    private final MessagePack msgPack;

    public MessagePackDeserializerFactory(MessagePack msgPack) {
        this.msgPack = msgPack;
    }

    @Override
    public <T> CocainePayloadDeserializer<T> createDeserializer(Class<T> clazz) {
        return new MessagePackPayloadDeserializer(msgPack, clazz);
    }
}
