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

import java.nio.charset.Charset;

final class HpackUtil {

  static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");

  /**
   * A string compare that doesn't leak timing information.
   */
  static boolean equals(byte[] s1, byte[] s2) {
    if (s1.length != s2.length) {
      return false;
    }
    char c = 0;
    for (int i = 0; i < s1.length; i++) {
      c |= (s1[i] ^ s2[i]);
    }
    return c == 0;
  }

  /**
   * Checks that the specified object reference is not {@code null}.
   */
  static <T> T requireNonNull(T obj) {
    if (obj == null)
      throw new NullPointerException();
    return obj;
  }



}
