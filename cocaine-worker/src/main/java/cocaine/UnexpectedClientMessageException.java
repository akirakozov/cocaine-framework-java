package cocaine;

import cocaine.message.WorkerMessage;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnexpectedClientMessageException extends ClientException {

    private final WorkerMessage msg;

    public UnexpectedClientMessageException(WorkerMessage msg) {
        super("Unexpected message: " + msg.toString());
        this.msg = msg;
    }

    public WorkerMessage getMsg() {
        return msg;
    }
}
