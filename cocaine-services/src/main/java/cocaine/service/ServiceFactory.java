package cocaine.service;

import cocaine.*;
import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
import cocaine.locator.Locator;
import cocaine.service.invocation.AdditionalHeadersAppender;
import cocaine.service.invocation.IdentityHeadersAppender;
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
        return createService(type, new ServiceOptions(), new IdentityHeadersAppender());
    }

    public <T extends AutoCloseable> T createService(Class<T> type, ServiceOptions options) {
        Service service = locator.service(getServiceName(type), options, new IdentityHeadersAppender());
        return create(type, new ServiceMethodHandler(service));
    }

    public <T extends AutoCloseable> T createService(Class<T> type, ServiceOptions options,
            AdditionalHeadersAppender appender)
    {
        Service service = locator.service(getServiceName(type), options, appender);
        return create(type, new ServiceMethodHandler(service));
    }

    public <T extends AutoCloseable> T createApp(Class<T> type, ServiceOptions options,
            AdditionalHeadersAppender appender)
    {
        Service service = locator.service(getServiceName(type), options, appender);
        return create(type, new AppServiceMethodHandler(service));
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
        return isNoArgMethodWithName(method, "toString");
    }

    private static boolean isClose(Method method) {
        return isNoArgMethodWithName(method, "close");
    }

    private static boolean isNoArgMethodWithName(Method method, String name) {
        return method.getName().equals(name) && method.getParameterTypes().length == 0;
    }

    private static Class<?> getResultType(TypeToken<?> returnType, boolean isPrimitiveMethod) {
        if (isPrimitiveMethod) {
            return returnType.getRawType();
        } else {
            Preconditions.checkArgument(Session.class.isAssignableFrom(returnType.getRawType()),
                    "Method result type should be parametrized Session<> value");
            Preconditions.checkArgument(returnType.getType() instanceof ParameterizedType,
                    "Method result type should be parametrized Session<> value");
            ParameterizedType type = (ParameterizedType) returnType.getType();
            return (Class<?>) type.getActualTypeArguments()[0];
        }
    }

    private CocainePayloadDeserializer findDeserializer(CocaineMethod methodDescriptor, Class<?> resultClass) {
        if (Void.class.isAssignableFrom(resultClass) || resultClass.equals(Void.TYPE)) {
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
            } else if (isClose(thisMethod)) {
                service.close();
                return null;
            }

            final CocaineMethod methodDescriptor = Preconditions.checkNotNull(
                    thisMethod.getAnnotation(CocaineMethod.class),
                    "Service method must be annotated with @CocaineMethod annotation");
            String method = getMethod(thisMethod);
            boolean isSimpleMethod = service.isPrimitiveOrIdentityApiMethod(method);

            Invokable<?, Object> invokable = Invokable.from(thisMethod);
            Parameter[] parameters = Iterables.toArray(invokable.getParameters(), Parameter.class);
            Class<?> resultClass = getResultType(invokable.getReturnType(), isSimpleMethod);
            CocainePayloadDeserializer deserializer = findDeserializer(methodDescriptor, resultClass);

            List<Object> arguments = getArgs(thisMethod, parameters, args);

            if (isSimpleMethod) {
                // return value directly for primitive value-error methods
                try (Session<?> session = service.invoke(method, deserializer, arguments)) {
                    return session.rx().get();
                }
            } else {
                return service.invoke(method, deserializer, arguments);
            }
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
            CocaineMethod methodDescriptor = Preconditions.checkNotNull(method.getAnnotation(CocaineMethod.class),
                    "Service method must be annotated with @CocaineMethod annotation");
            return methodDescriptor.value().isEmpty() ? method.getName() : methodDescriptor.value();
        }

        @Override
        protected List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) {
            return Arrays.asList(args);
        }
    }

    private class AppServiceMethodHandler extends CocaineMethodHandler {

        public AppServiceMethodHandler(Service service) {
            super(service);
        }

        @Override
        protected String getMethod(Method method) {
            return "enqueue";
        }

        @Override
        protected List<Object> getArgs(Method method, Parameter[] parameters, Object[] args) throws IOException {
            CocaineMethod methodDescriptor = Preconditions.checkNotNull(method.getAnnotation(CocaineMethod.class),
                    "AppService method must be annotated with @CocaineMethod annotation");

            String name = methodDescriptor.value().isEmpty() ? method.getName() : methodDescriptor.value();
            CocaineSerializer serializer =
                    serializers.stream()
                    .filter(s -> methodDescriptor.serializer().isInstance(s))
                    .findFirst()
                    .orElseThrow(() -> new RuntimeException("Couldn't find serializer"));

            return Arrays.asList(name, serializer.serialize(parameters, args));
        }
    }
}
