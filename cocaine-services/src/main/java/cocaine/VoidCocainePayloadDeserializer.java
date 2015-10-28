package cocaine;

import cocaine.session.CocainePayloadDeserializer;
import org.msgpack.type.Value;

import java.io.IOException;

/**
 * @author akirakozov
 */
public class VoidCocainePayloadDeserializer implements CocainePayloadDeserializer<Void> {

    @Override
    public Void deserialize(String messageType, Value payload) throws IOException {
        return null;
    }
}
