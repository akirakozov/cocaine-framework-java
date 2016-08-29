package cocaine.unistorage.errors;

import cocaine.ServiceException;

/**
 * @author metal
 */
public class StorageResponseError extends ServiceException {
    public StorageResponseError(String service, StorageErrorType type) {
        super(service, type.toString());
    }
}
