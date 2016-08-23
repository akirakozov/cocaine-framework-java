package cocaine.unistorage;

/**
 * @author metal
 */
public class FileMetaResponse extends StorageResponse {
    private final String attributes;
    private final String key;
    private final long size;
    private final long timestamp;

    public FileMetaResponse(String attributes, String key, long size, long timestamp) {
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

    public long getSize() {
        return size;
    }

    public long getTimestamp() {
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
