package cocaine;

import cocaine.message.WorkerMessage;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnexpectedClientMessageException extends ClientException {

    private final WorkerMessage msg;

    public UnexpectedClientMessageException(String application, WorkerMessage msg) {
        super(application, "Unexpected message: " + msg.toString());
        this.msg = msg;
    }

    public WorkerMessage getMsg() {
        return msg;
    }
}
