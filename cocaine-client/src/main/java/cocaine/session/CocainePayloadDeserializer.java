package cocaine.session;

import org.msgpack.type.Value;

import java.io.IOException;

/**
 * @author akirakozov
 */
public interface CocainePayloadDeserializer<T> {
    T deserialize(String messageType, Value payload) throws IOException;

}
