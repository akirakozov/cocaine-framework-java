package cocaine.message;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 * @author akirakozov
 */
public final class ErrorMessage extends WorkerMessage {

    public static final class Code {

        // No handler for requested event
        public static final int ENOHANDLER = 200;
        // Invocation failed
        public static final int EINVFAILED = 212;
        // Service is disconnected
        public static final int ESRVDISCON = 220;

        private Code() { }
    }

    public static final class Category {
        public static final int FRAMEWORK = 42;

        private Category() {}
    }

    private final int code;
    private final int category;
    private final String message;

    public ErrorMessage(long session, List<List<Object>> headers, int category, int code, String message) {
        super(MessageType.ERROR, session, headers);
        this.code = code;
        this.category = category;
        this.message = Preconditions.checkNotNull(message, "Error message can not be null");
    }

    public int getCode() {
        return code;
    }

    public int getCategory() {
        return category;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "ErrorMessage/" + getSession() + ": " + category + ", " + code + " - " + message;
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

        ErrorMessage that = (ErrorMessage) o;
        return code == that.code && category == that.category && message.equals(that.message);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + category;
        result = 31 * result + code;
        result = 31 * result + message.hashCode();
        return result;
    }

}
