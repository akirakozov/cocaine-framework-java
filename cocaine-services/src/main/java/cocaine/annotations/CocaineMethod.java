package cocaine.annotations;

import cocaine.CocainePayloadDeserializerFactory;
import cocaine.CocaineSerializer;
import cocaine.MessagePackDeserializerFactory;
import cocaine.MessagePackSerializer;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author akirakozov
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface CocaineMethod {

    String value() default "";

    Class<? extends CocaineSerializer> serializer() default MessagePackSerializer.class;

    Class<? extends CocainePayloadDeserializerFactory> deserializerFactory()
            default MessagePackDeserializerFactory.class;

}
