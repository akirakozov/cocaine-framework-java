package cocaine.unistorage.errors;

import cocaine.ServiceException;

/**
 * @author metal
 */
public class StorageResponseError extends ServiceException {
    public final StorageErrorType type;

    public StorageResponseError(String service, StorageErrorType type, String message) {
        super(service, type.toString() + ": " + message);
        this.type = type;
    }
}
