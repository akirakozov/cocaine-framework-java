package cocaine.message;

import cocaine.hpack.HeaderField;

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

    public static WorkerMessage invoke(long session, List<HeaderField> headers, String event) {
        return new InvokeMessage(session, headers, event);
    }

    public static WorkerMessage write(long session, byte[] data, List<HeaderField> headers) {
        return new WriteMessage(session, data, headers);
    }

    public static WorkerMessage close(long session, List<HeaderField> headers) {
        return new CloseMessage(session, headers);
    }

    public static WorkerMessage error(long session, List<HeaderField> headers, int category, int code, String message) {
        return new ErrorMessage(session, headers, category, code, message);
    }

    private Messages() { }
}
