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

import static cocaine.hpack.HpackUtil.ISO_8859_1;
import static cocaine.hpack.HpackUtil.requireNonNull;

public class HeaderField implements Comparable<HeaderField> {

    // Section 4.1. Calculating Table Size
    // The additional 32 octets account for an estimated
    // overhead associated with the structure.
    static final int HEADER_ENTRY_OVERHEAD = 32;

    public final byte[] name;
    public final byte[] value;

    // This constructor can only be used if name and value are ISO-8859-1 encoded.
    public HeaderField(String name, String value) {
        this(name.getBytes(ISO_8859_1), value.getBytes(ISO_8859_1));
    }

    public HeaderField(byte[] name, byte[] value) {
        this.name = requireNonNull(name);
        this.value = requireNonNull(value);
    }

    public static byte[] valueFromInt(int value) {
        return new byte[]{
                (byte) (value >>> 24),
                (byte) (value >>> 16),
                (byte) (value >>> 8),
                (byte) value};
    }

    public static int toInt(byte[] value) {
        if (value.length != 8) {
            return 0;
        }
        return (value[0] << 24) + (value[1] << 16) + (value[2] << 8) + value[3];
    }

    int size() {
        return name.length + value.length + HEADER_ENTRY_OVERHEAD;
    }

    @Override
    public int compareTo(HeaderField anotherHeaderField) {
        int ret = compareTo(name, anotherHeaderField.name);
        if (ret == 0) {
            ret = compareTo(value, anotherHeaderField.value);
        }
        return ret;
    }

    private int compareTo(byte[] s1, byte[] s2) {
        int len1 = s1.length;
        int len2 = s2.length;
        int lim = Math.min(len1, len2);

        int k = 0;
        while (k < lim) {
            byte b1 = s1[k];
            byte b2 = s2[k];
            if (b1 != b2) {
                return b1 - b2;
            }
            k++;
        }
        return len1 - len2;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof HeaderField)) {
            return false;
        }
        HeaderField other = (HeaderField) obj;
        boolean nameEquals = HpackUtil.equals(name, other.name);
        boolean valueEquals = HpackUtil.equals(value, other.value);
        return nameEquals && valueEquals;
    }

    @Override
    public String toString() {
        String nameString = new String(name);
        String valueString = new String(value);
        return nameString + ": " + valueString;
    }
}
