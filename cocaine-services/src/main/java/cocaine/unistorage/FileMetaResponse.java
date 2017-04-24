package cocaine.unistorage;

import java.util.Optional;

/**
 * @author metal
 */
public class FileMetaResponse extends StorageResponse {
    private final String attributes;
    private final String key;
    private final Optional<Long> size;
    private final Optional<Long> timestamp;

    public FileMetaResponse(String attributes, String key, Optional<Long> size, Optional<Long> timestamp) {
        this.attributes = attributes;
        this.key = key;
        this.size = size;
        this.timestamp = timestamp;
    }

    public String getAttributes() {
        return attributes;
    }

    public String getKey() {
        return key;
    }

    public Optional<Long> getSizeO() {
        return size;
    }

    public Optional<Long> getTimestampO() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "FileMetaResponse["
                + "attributes=" + attributes + ","
                + "key=" + key + ","
                + "size=" + size + ","
                + "timestamp=" + timestamp
                + "]";
    }
}
