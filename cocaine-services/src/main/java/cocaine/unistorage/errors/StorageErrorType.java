package cocaine.unistorage.errors;

/**
 * @author metal
 */
public enum StorageErrorType {
    TOO_SMALL_CHUNK_SIZE(1),
    TOO_BIG_CHUNK_SIZE(2),
    INVALID_NAMESPACE(3),
    INVALID_KEY(4),
    INVALID_COUPLE(5),
    FILE_NOT_FOUND(6),
    NO_SUITABLE_BACKEND_FOUND(7),

    INTERNAL_UNISTORAGE_ERROR(100),
    BACKEND_IO_ERROR(101),
    FILE_CORRUPTED(102),
    UNKNOWN_UNISTORAGE_ERROR(103),

    GENERIC_TOKEN_ERROR(200),
    INVALID_CREDENTIALS(201),
    INVALID_TOKEN_TYPE(202),
    INVALID_UIDS_COUNT(203);

    public final int ordinal;

    StorageErrorType(final int ordinal) {
        this.ordinal = ordinal;
    }

    public boolean isRequestError() {
        return ordinal < 100;
    }

    public boolean isProcessingError() {
        return 100 <= ordinal && ordinal < 200;
    }

    public boolean isAuthError() {
        return 200 <= ordinal;
    }

    public static StorageErrorType byOrdinal(int ordinal) {
        for (StorageErrorType e : StorageErrorType.values()) {
            if (e.ordinal == ordinal) {
                return e;
            }
        }
        return null;
    }
}
