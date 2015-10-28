package cocaine;

import cocaine.session.CocainePayloadDeserializer;

/**
 * @author akirakozov
 */
public interface CocainePayloadDeserializerFactory {

    <T> CocainePayloadDeserializer<T> createDeserializer(Class<T> clazz);
}
