package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class UnknownClientMethodException extends ClientException {
    private final String method;

    public UnknownClientMethodException(String method) {
        super("Unknown application method: " + method);
        this.method = method;
    }

    public String getMethod() {
        return method;
    }
}
