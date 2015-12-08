package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownClientMethodException extends ClientException {

    public UnknownClientMethodException(String method) {
        super("Unknown application method: " + method);
    }

}
