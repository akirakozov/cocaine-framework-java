package cocaine;

import java.io.IOException;
import java.util.Optional;

import cocaine.unistorage.DataResponse;
import cocaine.unistorage.FileMetaResponse;
import cocaine.unistorage.StorageResponse;
import cocaine.unistorage.errors.StorageErrorType;
import cocaine.unistorage.errors.StorageResponseError;
import org.msgpack.packer.Packer;
import org.msgpack.template.AbstractTemplate;
import org.msgpack.template.Template;
import org.msgpack.type.ArrayValue;
import org.msgpack.type.IntegerValue;
import org.msgpack.type.MapValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueFactory;
import org.msgpack.type.ValueType;
import org.msgpack.unpacker.Unpacker;

/**
 * @author metal
 */
public class StorageResponseTemplate extends AbstractTemplate<StorageResponse> {
    private static final String STORAGE_SERVICE_NAME = "unistorage";

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
                    toOptionalLong(mapValue.get(ValueFactory.createRawValue("size"))),
                    toOptionalLong(mapValue.get(ValueFactory.createRawValue("timestamp_ms")))
            );
        } else if (type == ValueType.RAW) {
            return new DataResponse(unpacker.readByteArray());
        } else if (type == ValueType.ARRAY) {
            ArrayValue arrayValue = (ArrayValue) unpacker.readValue();

            int errorCode = arrayValue.get(0).asArrayValue().get(1).asIntegerValue().getInt();
            StorageErrorType storageErrorType = StorageErrorType.byOrdinal(errorCode);

            String message = arrayValue.size() == 2 ? arrayValue.get(1).toString() : "";

            throw new StorageResponseError(STORAGE_SERVICE_NAME, storageErrorType, message);
        } else {
            throw new IllegalArgumentException("Can't parse storage response");
        }
    }

    private static Optional<Long> toOptionalLong(Value value) {
        return Optional.ofNullable(value)
                .map(Value::asIntegerValue)
                .map(IntegerValue::getLong);
    }
}
