package cocaine;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import com.beust.jcommander.JCommander;
import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Maps;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;

/**
 * @author Anton Bobukh <anton@bobukh.ru>
 */
public final class Runner {

    private static final Logger logger = Logger.getLogger(Runner.class);

    public static void run(Invoker invoker, String[] args) {
        WorkerOptions options = new WorkerOptions();
        new JCommander(options).parse(args);

        logger.info("Running " + options.getApplication() + " application");

        try (Worker worker = new Worker(options, invoker)) {
            worker.run();
            worker.join();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void run(Map<String, EventHandler> handlers, String[] args) {
        run(DefaultInvoker.createFromHandlers(handlers), args);
    }

    public static void run(Object container, String[] args) {
        Method[] methods = container.getClass().getDeclaredMethods();
        Map<String, EventHandler> handlers = new HashMap<>();
        for (Method method : methods) {
            Handler handler = method.getAnnotation(Handler.class);
            if (handler != null) {
                String name = handler.value().isEmpty() ? method.getName() : handler.value();
                handlers.put(name, MethodEventHandler.wrap(container, method));
            }
        }
        Runner.run(handlers, args);
    }

    private static final class MethodEventHandler implements EventHandler {

        private final Method method;
        private final Object container;

        private MethodEventHandler(Method method, Object container) {
            this.method = method;
            this.container = container;
        }

        public static MethodEventHandler wrap(Object container, Method method) {
            return new MethodEventHandler(method, container);
        }

        @Override
        public void handle(Observable<byte[]> request, Observer<byte[]> response) throws Exception {
            method.invoke(container, request, response);
        }

    }

    private Runner() { }

}
