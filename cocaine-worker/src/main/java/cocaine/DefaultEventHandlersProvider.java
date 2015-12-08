package cocaine;

import java.util.HashMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class DefaultEventHandlersProvider implements EventHandlersProvider {
    private final Map<String, EventHandler> handlers;

    public DefaultEventHandlersProvider() {
        this(new HashMap<>());
    }

    public DefaultEventHandlersProvider(Map<String, EventHandler> handlers) {
        this.handlers = handlers;
    }

    @Override
    public EventHandler getHandler(String event) {
        return handlers.get(event);
    }

    public void registerHandler(String event, EventHandler handler) {
        handlers.put(event, handler);
    }
}
