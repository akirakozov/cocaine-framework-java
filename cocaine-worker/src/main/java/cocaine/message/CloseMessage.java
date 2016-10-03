package cocaine.message;

import java.util.List;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class CloseMessage extends WorkerMessage {

    public CloseMessage(long session, List<List<Object>> headers) {
        super(MessageType.CLOSE, session, headers);
    }

    @Override
    public String toString() {
        return "CloseMessage/" + getSession();
    }

}
