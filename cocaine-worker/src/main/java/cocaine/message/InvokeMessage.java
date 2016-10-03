package cocaine.message;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class InvokeMessage extends WorkerMessage {

    private final String event;

    public InvokeMessage(long session, List<List<Object>> headers, String event) {
        super(MessageType.INVOKE, session, headers);
        this.event = Preconditions.checkNotNull(event, "Event can not be null");
    }

    public String getEvent() {
        return event;
    }

    @Override
    public String toString() {
        return "InvokeMessage/" + getSession() + ": " + event;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        InvokeMessage that = (InvokeMessage) o;
        return event.equals(that.event);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + event.hashCode();
        return result;
    }

}
