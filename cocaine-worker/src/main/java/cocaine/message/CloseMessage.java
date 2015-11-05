package cocaine.message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public final class CloseMessage extends Message {

    public CloseMessage(long session) {
        super(MessageType.CLOSE, session);
    }

    @Override
    public String toString() {
        return "CloseMessage/" + getSession();
    }

}
