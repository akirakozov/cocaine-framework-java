package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class HeartbeatMessage extends WorkerMessage {

    public HeartbeatMessage() {
        super(MessageType.HEARTBEAT);
    }

    @Override
    public String toString() {
        return "HeartbeatMessage/" + getSession();
    }

}
