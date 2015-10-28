package cocaine;

import cocaine.message.Message;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 * @author akirakozov
 */
public class UnexpectedServiceMessageException extends ServiceException {
    private final int msgType;

    public UnexpectedServiceMessageException(String service, int msgType) {
        super(service, "Unexpected message: " + msgType);
        this.msgType = msgType;
    }

    public int getMsgType() {
        return msgType;
    }
}
