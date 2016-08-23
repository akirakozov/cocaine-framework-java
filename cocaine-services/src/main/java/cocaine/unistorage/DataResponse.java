package cocaine.unistorage;

/**
 * @author metal
 */
public class DataResponse extends StorageResponse {
    private final byte[] data;

    public DataResponse(byte[] data) {
        this.data = data;
    }

    public byte[] getData() {
        return data;
    }

    @Override
    public String toString() {
        return "DataResponse["
                + "size=" + data.length
                + "]";
    }
}
