package cocaine.message;

import java.util.List;
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

    public static WorkerMessage invoke(long session, List<List<Object>> headers, String event) {
        return new InvokeMessage(session, headers, event);
    }

    public static WorkerMessage write(long session, byte[] data, List<List<Object>> headers) {
        return new WriteMessage(session, data, headers);
    }

    public static WorkerMessage close(long session, List<List<Object>> headers) {
        return new CloseMessage(session, headers);
    }

    public static WorkerMessage error(long session, List<List<Object>> headers, int category, int code, String message) {
        return new ErrorMessage(session, headers, category, code, message);
    }

    private Messages() { }
}
