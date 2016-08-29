package cocaine.unistorage.errors;

/**
 * @author metal
 */
public enum StorageErrorType {
    TOO_SMALL_CHUNK_SIZE(1),
    TOO_BIG_CHUNK_SIZE(2),
    INVALID_NAMESPACE(3),
    INVALID_CHUNK_SIZE_FROM_MULCAGATE(4),
    MISSING_SIZE_HEADER_FROM_MULCAGATE(5),
    INVALID_SIZE_HEADER_FROM_MULCAGATE(6);

    public final int ordinal;

    StorageErrorType(final int ordinal) {
        this.ordinal = ordinal;
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
