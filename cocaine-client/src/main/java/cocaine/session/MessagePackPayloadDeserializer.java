package cocaine.session;

import org.msgpack.MessagePack;
import org.msgpack.type.Value;
import org.msgpack.unpacker.Converter;

import java.io.IOException;

/**
 * @author akirakozov
 */
public class MessagePackPayloadDeserializer<T> implements CocainePayloadDeserializer<T> {
    private final Class<T> clazz;
    private final MessagePack pack;

    public MessagePackPayloadDeserializer(MessagePack pack, Class<T> clazz) {
        this.clazz = clazz;
        this.pack = pack;
    }

    @Override
    public T deserialize(String messageType, Value payload) throws IOException {
        return new Converter(payload).read(pack.lookup(clazz));
    }
}
