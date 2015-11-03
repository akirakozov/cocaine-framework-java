package cocaine.message;

import java.util.UUID;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Messages {

    public static Message handshake(UUID id) {
        return new HandshakeMessage(id);
    }

    public static Message heartbeat() {
        return new HeartbeatMessage();
    }

    public static Message terminate(TerminateMessage.Reason reason, String message) {
        return new TerminateMessage(reason, message);
    }

    public static Message invoke(long session, String event) {
        return new InvokeMessage(session, event);
    }

    public static Message chunk(long session, byte[] data) {
        return new WriteMessage(session, data);
    }

    public static Message choke(long session) {
        return new CloseMessage(session);
    }

    public static Message error(long session, int code, String message) {
        return new ErrorMessage(session, code, message);
    }

    private Messages() { }
}
