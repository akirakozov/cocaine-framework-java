package cocaine.message;

import java.util.UUID;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class Messages {

    public static WorkerMessage handshake(UUID id) {
        return new HandshakeMessage(id);
    }

    public static WorkerMessage heartbeat() {
        return new HeartbeatMessage();
    }

    public static WorkerMessage terminate(TerminateMessage.Reason reason, String message) {
        return new TerminateMessage(reason, message);
    }

    public static WorkerMessage invoke(long session, String event) {
        return new InvokeMessage(session, event);
    }

    public static WorkerMessage write(long session, byte[] data) {
        return new WriteMessage(session, data);
    }

    public static WorkerMessage close(long session) {
        return new CloseMessage(session);
    }

    public static WorkerMessage error(long session, int code, String message) {
        return new ErrorMessage(session, code, message);
    }

    private Messages() { }
}
