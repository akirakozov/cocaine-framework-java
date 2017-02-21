package cocaine;

import cocaine.annotations.CocaineMethod;
import cocaine.annotations.CocaineService;
import cocaine.session.Session;
import cocaine.unistorage.StorageResponse;

import java.util.Map;

/**
 * @author metal
 */
@CocaineService("unistorage")
public interface Unistorage extends AutoCloseable {
    @CocaineMethod("read")
    Session<StorageResponse> read(String ns, String key, long coupleId, long offset, long size, long chunkSize);

    @CocaineMethod("read2")
    Session<StorageResponse> read2(String key, long offset, long size, long chunkSize, Map<String, String> params);
}
