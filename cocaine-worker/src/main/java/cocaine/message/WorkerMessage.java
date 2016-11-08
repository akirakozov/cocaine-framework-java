package cocaine.message;

import cocaine.hpack.HeaderField;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public abstract class WorkerMessage {

    private static final long SYSTEM_SESSION = 1L;

    private final MessageType type;
    private final long session;
    private final List<HeaderField> headers;

    protected WorkerMessage(MessageType type, long session, List<HeaderField> headers) {
        this.type = type;
        this.session = session;
        this.headers = headers;
    }

    protected WorkerMessage(MessageType type) {
        this(type, SYSTEM_SESSION, new ArrayList<>());
    }

    public MessageType getType() {
        return type;
    }

    public long getSession() {
        return session;
    }

    public List<HeaderField> getHeaders() {
        return headers;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        WorkerMessage message = (WorkerMessage) o;
        return session == message.session && type == message.type;
    }

    @Override
    public int hashCode() {
        int result = type.hashCode();
        result = 31 * result + (int) (session ^ (session >>> 32));
        return result;
    }

}
