/*
 * Copyright 2014 Twitter, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package cocaine.hpack;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.msgpack.type.ArrayValue;
import org.msgpack.type.Value;
import org.msgpack.type.ValueType;


public final class Decoder {
    private static final IOException DECOMPRESSION_EXCEPTION = new IOException("decompression failure");

    private final DynamicTable dynamicTable;

    public static final int DEFAULT_TABLE_SIZE = 4096;

    public Decoder(int maxHeaderTableSize) {
        dynamicTable = new DynamicTable(maxHeaderTableSize);
    }

    public List<HeaderField> decode(Value rawData) throws IOException {
        if (!rawData.isArrayValue()) {
            throw DECOMPRESSION_EXCEPTION;
        }

        List<HeaderField> parsedHeaders = new ArrayList<>();

        ArrayValue rawHeaders = rawData.asArrayValue();
        for (Value rawHeader : rawHeaders) {
            if (rawHeader.isIntegerValue()) {
                int index = rawHeader.asIntegerValue().getInt();
                HeaderField header = getByIndex(index);
                parsedHeaders.add(header);
            } else if (rawHeader.getType() == ValueType.ARRAY) {
                Value[] headerArray = rawHeader.asArrayValue().getElementArray();
                if (headerArray.length != 3) {
                    throw DECOMPRESSION_EXCEPTION;
                }
                byte[] name;
                byte[] value;
                if (headerArray[1].isIntegerValue()) {
                    name = getByIndex(headerArray[1].asIntegerValue().getInt()).name;
                } else {
                    name = headerArray[1].asRawValue().getByteArray();
                }
                value = headerArray[2].asRawValue().getByteArray();
                HeaderField header = new HeaderField(name, value);
                if (headerArray[0].asBooleanValue().getBoolean()) {
                    dynamicTable.add(header);
                }
                parsedHeaders.add(header);
            }
        }
        return parsedHeaders;
    }

    private HeaderField getByIndex(int index) {
        return index <= StaticTable.LENGTH
                ? StaticTable.getEntry(index)
                : dynamicTable.getEntry(index - StaticTable.LENGTH);
    }
}
