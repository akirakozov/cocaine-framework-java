package cocaine.http.invoker;

import cocaine.EventHandler;
import cocaine.EventHandlersProvider;
import cocaine.Invoker;
import cocaine.UnknownClientMethodException;
import cocaine.request.RequestIdStack;
import org.apache.log4j.Logger;
import rx.Observable;
import rx.Observer;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author akirakozov
 */
public class MultiThreadInvoker implements Invoker {
    private static final Logger logger = Logger.getLogger(MultiThreadInvoker.class);

    private final ExecutorService executor;
    private final EventHandlersProvider provider;

    public MultiThreadInvoker(int threadsCount, EventHandlersProvider provider) {
        this(threadsCount, "MTInvoker", provider);
    }

    public MultiThreadInvoker(int threadsCount, String threadsNamePrefix, EventHandlersProvider provider) {
        this.executor = Executors.newFixedThreadPool(threadsCount, new NamedThreadFactory(threadsNamePrefix));
        this.provider = provider;
    }

    @Override
    public void invoke(String event, List<List<Object>> headers, Observable<byte[]> request, Observer<byte[]> response) throws Exception {
        EventHandler handler = provider.getHandler(event);
        if (handler != null) {
            executor.execute( () -> {
                try {
                    headers.forEach(RequestIdStack::pushReplaceId);
                    handler.handle(request, response);
                } catch (Exception e) {
                    logger.warn(e, e);
                } finally {
                    RequestIdStack.pop();
                }
            });
        } else {
            throw new UnknownClientMethodException(event);
        }
    }

    public ExecutorService getExecutor() {
        return executor;
    }
}
