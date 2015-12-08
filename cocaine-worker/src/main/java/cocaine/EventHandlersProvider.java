package cocaine;

/**
 * @author akirakozov
 */
public interface EventHandlersProvider {
    EventHandler getHandler(String event);
}
