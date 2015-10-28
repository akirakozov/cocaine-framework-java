package cocaine;

import cocaine.annotations.CocaineMethodV12;
import cocaine.annotations.CocaineService;
import cocaine.locator.Locator;
import cocaine.service.Service;
import cocaine.session.CocainePayloadDeserializer;
import cocaine.session.Session;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.Parameter;
import com.google.common.reflect.TypeToken;
import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;
import org.msgpack.MessagePack;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.List;

/**
 * @author akirakozov
 */
public class ServiceFactory {
    private static final Method close;

    static {
        try {
            close = AutoCloseable.class.getMethod("close");
        } catch (NoSuchMethodException e) {
            throw Throwables.propagate(e);
        }
    }

    private final Locator locator;
    private final List<CocaineSerializer> serializers;
    private final List<CocainePayloadDeserializerFactory> deserializerFactories;

    public ServiceFactory(Locator locator) {
        this(locator, new MessagePack());
    }
    public ServiceFactory(Locator locator, MessagePack msgPack) {
        this.locator = locator;
        this.serializers = ImmutableList.of(new MessagePackSerializer(msgPack));
        this.deserializerFactories = ImmutableList.of(new MessagePackDeserializerFactory(msgPack));
    }

    public <T extends AutoCloseable> T createService(Class<T> type) {
        Service service = locator.service(getServiceName(type));
        return create(type, new ServiceMethodHandler(service));
    }

    private static <T> String getServiceName(Class<T> type) {
        CocaineService service = Preconditions.checkNotNull(type.getAnnotation(CocaineService.class),
                "Service interface must be annotated with @CocaineService annotation");
        return service.value();
    }

    @SuppressWarnings("unchecked")
    private static <T extends AutoCloseable> T create(Class<T> type, MethodHandler handler) {
        Preconditions.checkArgument(type.isInterface(), "Service must be described with interface");
        try {
            ProxyObject instance = createType(type).newInstance();
            instance.setHandler(handler);
            return (T) instance;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new ServiceInstantiationException(type, e);
        }
    }

    @SuppressWarnings("unchecked")
    private static <T extends AutoCloseable> Class<? extends ProxyObject> createType(Class<T> type) {
        ProxyFactory factory = new ProxyFactory();
        factory.setInterfaces(new Class[]{type});
        return factory.createClass();
    }

    private static boolean isToString(Method method) {
        return method.getName().equals("toString") && method.getParameterTypes().length == 0;
    }

    private static Class<?> getResultType(TypeToken<?> returnType) {
        Preconditions.checkArgument(Session.class.isAssignableFrom(returnType.getRawType()),
                "Method result type should be parametrized Session<> value");
        Preconditions.checkArgument(returnType.getType() instanceof ParameterizedType,
                "Method result type should be parametrized Session<> value");
        ParameterizedType type = (ParameterizedType) returnType.getType();
        return (Class<?>) type.getActualTypeArguments()[0];
    }

    private CocainePayloadDeserializer findDeserializer(CocaineMethodV12 methodDescriptor, Class<?> resultClass) {
        if (Void.class.isAssignableFrom(resultClass)) {
            return new VoidCocainePayloadDeserializer();
        } else {
            return deserializerFactories.stream()
                    .filter(f -> methodDescriptor.deserializerFactory().isInstance(f))
                    .findFirst()
                    .map(f -> f.createDeserializer(resultClass))
                    .orElseThrow(() -> new RuntimeException("Couldn't find deserializer"));
        }
    }

    private abstract class CocaineMethodHandler implements MethodHandler {

        private final Service service;

        protected CocaineMethodHandler(Service service) {
            this.service = service;
        }

        @Override
        public Object invoke(Object self, Method thisMethod, Method proceed, Object[] args) throws Throwable {
            if (isToString(thisMethod)) {
                return service.toString();
            } else if (thisMethod.equals(close)) {
                service.close();
                return null;
            }

            final CocaineMethodV12 methodDescriptor = Preconditions.checkNotNull(
                    thisMethod.getAnnotation(CocaineMethodV12.class),
                    "Service method must be annotated with @CocaineMethod annotation");

            Invokable<?, Object> invokable = Invokable.from(thisMethod);
            Parameter[] parameters = Iterables.toArray(invokable.getParameters(), Parameter.class);
            Class<?> resultClass = getResultType(invokable.getReturnType());
            CocainePayloadDeserializer deserializer = findDeserializer(methodDescriptor, resultClass);


            String method = getMethod(thisMethod);
            List<Object> arguments = getArgs(thisMethod, parameters, args);

            return service.invoke(method, deserializer, arguments);
        }

        protected abstract String getMethod(Method method);

        protected abstract List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) throws IOException;

    }

    private class ServiceMethodHandler extends CocaineMethodHandler {

        public ServiceMethodHandler(Service service) {
            super(service);
        }

        @Override
        protected String getMethod(Method method) {
            CocaineMethodV12 methodDescriptor = Preconditions.checkNotNull(method.getAnnotation(CocaineMethodV12.class),
                    "Service method must be annotated with @CocaineMethod annotation");
            return methodDescriptor.value().isEmpty() ? method.getName() : methodDescriptor.value();
        }

        @Override
        protected List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) {
            return Arrays.asList(args);
        }
    }

}
