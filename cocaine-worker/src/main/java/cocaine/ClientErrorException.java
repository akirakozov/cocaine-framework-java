package cocaine;

/**
 * @author Anton Bobukh <abobukh@yandex-team.ru>
 */
public class ClientErrorException extends ClientException {

    private final int code;

    public ClientErrorException(String message, int code) {
        super(code + " - " + message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
