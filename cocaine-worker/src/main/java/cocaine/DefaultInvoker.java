package cocaine;

import rx.Observable;
import rx.Observer;

/**
 * @author akirakozov
 */
public class DefaultInvoker implements Invoker {
    private final EventHandlersProvider provider;

    public DefaultInvoker(EventHandlersProvider provider) {
        this.provider = provider;
    }

    @Override
    public void invoke(String event, Observable<byte[]> request, Observer<byte[]> response) throws Exception {
        EventHandler handler = provider.getHandler(event);
        if (handler != null) {
            handler.handle(request, response);
        } else {
            throw new UnknownClientMethodException(event);
        }
    }
}
