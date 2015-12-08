package cocaine.http;

import cocaine.EventHandler;
import cocaine.EventHandlersProvider;

import javax.servlet.http.HttpServlet;
import java.util.HashMap;
import java.util.Map;

/**
 * @author akirakozov
 */
public class HttpEventHoldersProvider implements EventHandlersProvider {
    Map<String, EventHandler> handlers = new HashMap<>();

    @Override
    public EventHandler getHandler(String event) {
        return handlers.get(event);
    }

    public void registerServlet(String name, HttpServlet servlet) {
        handlers.put(name, new HttpEventHandler(servlet));
    }
}
