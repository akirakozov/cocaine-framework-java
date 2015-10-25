package cocaine.session;

import org.msgpack.type.Value;

import java.io.IOException;

/**
 * @author akirakozov
 */
public class ValueIdentityPayloadDeserializer implements CocainePayloadDeserializer<Value> {
    @Override
    public Value deserialize(String messageType, Value payload) throws IOException {
        return payload;
    }
}
