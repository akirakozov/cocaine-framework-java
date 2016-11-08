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

import org.msgpack.packer.Packer;

import java.io.IOException;

public final class Encoder {

  private final DynamicTable dynamicTable;

  /**
   * Creates a new encoder.
   */
  public Encoder(int maxHeaderTableSize) {
    dynamicTable = new DynamicTable(maxHeaderTableSize);
  }

  /**
   * Encode the header field into the header block.
   */
  public void encodeHeader(Packer packer, byte[] name, byte[] value, Boolean store) throws IOException {
    HeaderField headerField = new HeaderField(name, value);
    Boolean written = false;
    int name_idx = 0;
    for(int i = 1; i <= StaticTable.length; i++) {
      HeaderField entry = StaticTable.getEntry(i);
      if(entry == headerField) {
        packer.write(i);
        break;
      }
      if(HpackUtil.equals(entry.name, headerField.name)) {
        name_idx = i;
      }
    }
    if(written) {
      return;
    }

    for(int i = 1; i <= dynamicTable.length(); i++) {
      HeaderField entry = dynamicTable.getEntry(i);
      if(entry == headerField) {
        packer.write(i);
        break;
      }
      if(HpackUtil.equals(entry.name, headerField.name)) {
        name_idx = i;
      }
    }
    if(written) {
      return;
    }
    packer.writeArrayBegin(3);
    packer.write(store);
    if(name_idx != 0) {
      packer.write(name_idx);
    } else {
      packer.write(name);
    }
    packer.write(value);
    packer.writeArrayEnd();
  }

}
