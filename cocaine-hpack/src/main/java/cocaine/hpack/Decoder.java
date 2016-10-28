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
import org.msgpack.unpacker.Unpacker;

import static cocaine.hpack.HeaderField.HEADER_ENTRY_OVERHEAD;

public final class Decoder {

  private static final IOException DECOMPRESSION_EXCEPTION =
      new IOException("decompression failure");
  private static final IOException ILLEGAL_INDEX_VALUE =
      new IOException("illegal index value");
  private static final IOException INVALID_MAX_DYNAMIC_TABLE_SIZE =
      new IOException("invalid max dynamic table size");
  private static final IOException MAX_DYNAMIC_TABLE_SIZE_CHANGE_REQUIRED =
      new IOException("max dynamic table size change required");

  private final DynamicTable dynamicTable;

  public static final int DEFAULT_TABLE_SIZE = 4096;
  /**
   * Creates a new decoder.
   */
  public Decoder(int maxHeaderTableSize) {
    dynamicTable = new DynamicTable(maxHeaderTableSize);
  }

  /**
   * Decode the header block into header fields.
   */
  public List<HeaderField> decode(Value raw_data) throws IOException {
    if(!raw_data.isArrayValue()) {
      throw DECOMPRESSION_EXCEPTION;
    }
    List<HeaderField> parsedHeaders = new ArrayList<HeaderField>();
    ArrayValue raw_headers = raw_data.asArrayValue();
    for (int i = 0; i < raw_headers.size(); i++) {
      Value headerValue = raw_headers.get(i);
      if(headerValue.isIntegerValue()) {
        int idx = headerValue.asIntegerValue().getInt();
        HeaderField header = get_by_index(idx);
        parsedHeaders.add(header);
      } else if (headerValue.getType() == ValueType.ARRAY) {
        Value[] header_array = headerValue.asArrayValue().getElementArray();
        if(header_array.length != 3) {
          throw DECOMPRESSION_EXCEPTION;
        }
        byte[] name;
        byte[] value;
        if(header_array[1].isIntegerValue()) {
          name = get_by_index(header_array[1].asIntegerValue().getInt()).name;
        } else {
          name = header_array[1].asRawValue().getByteArray();
        }
        value = header_array[2].asRawValue().getByteArray();
        HeaderField header = new HeaderField(name, value);
        if(header_array[0].asBooleanValue().getBoolean()) {
          dynamicTable.add(header);
        }
        parsedHeaders.add(header);
      }
    }
    return parsedHeaders;
  }

  private HeaderField get_by_index(int idx) {
    if(idx <= StaticTable.length) {
      return StaticTable.getEntry(idx);
    } else {
      //TODO: is it ok to make shallow copy here?
      return dynamicTable.getEntry(idx - StaticTable.length);
    }
  }

}
