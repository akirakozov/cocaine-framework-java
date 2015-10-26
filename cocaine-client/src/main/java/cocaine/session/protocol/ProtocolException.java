package cocaine.session.protocol;

import cocaine.ServiceException;

/**
 * @author akirakozov
 */
public class ProtocolException extends ServiceException {
    public ProtocolException(String service, String message) {
        super(service, message);
    }
}
