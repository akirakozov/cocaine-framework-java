package cocaine;

import cocaine.unistorage.DataResponse;
import cocaine.unistorage.FileMetaResponse;
import cocaine.unistorage.StorageResponse;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.MapValue;
import org.msgpack.type.ValueFactory;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

import java.io.IOException;

/**
 * @author metal
 */
public class StorageResponseTemplate extends AbstractTemplate<StorageResponse> {

    private static final Template<StorageResponse> instance = new StorageResponseTemplate();

    private StorageResponseTemplate() { }

    public static Template<StorageResponse> getInstance() {
        return instance;
    }

    @Override
    public void write(Packer packer, StorageResponse response, boolean required) throws IOException {
        throw new UnsupportedOperationException("StorageResponseTemplate.write");
    }

    @Override
    public StorageResponse read(Unpacker unpacker, StorageResponse response, boolean required) throws IOException {
        ValueType type = unpacker.getNextType();

        if (type == ValueType.MAP) {
            MapValue mapValue = (MapValue) unpacker.readValue();

            return new FileMetaResponse(
                    mapValue.get(ValueFactory.createRawValue("attributes")).toString(),
                    mapValue.get(ValueFactory.createRawValue("key")).toString(),
                    mapValue.get(ValueFactory.createRawValue("size")).asIntegerValue().getLong(),
                    mapValue.get(ValueFactory.createRawValue("timestamp_ms")).asIntegerValue().getLong());
        } else if (type == ValueType.RAW) {
            return new DataResponse(unpacker.readByteArray());
        } else {
            throw new IllegalArgumentException("Can't parse storage response");
        }
    }
}
