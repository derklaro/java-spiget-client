/*
 * This file is part of spiget-java-client, licensed under the MIT License (MIT).
 *
 * Copyright (c) 2022 Pasqual K. and contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package dev.derklaro.spiget.client;

import dev.derklaro.spiget.Request;
import java.lang.invoke.MethodHandle;
import java.lang.reflect.Type;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
final class RequestInfo {

  private static final Object[] EMPTY = new Object[0];

  private final Type responseType;
  private final String contentType;
  private final MessageFormat format;
  private final String requestMethod;
  private final Collection<Map.Entry<String, MethodHandle>> queryFields;

  public @NonNull String formatUri(@NonNull Request<?> request, @NonNull Object... params) {
    StringBuffer buffer = new StringBuffer();
    // format the format for the uri
    String[] stringified = new String[params.length];
    for (int i = 0; i < params.length; i++) {
      stringified[i] = Objects.toString(params[i]);
    }
    this.format.format(stringified, buffer, new FieldPosition(0));

    // append the query parameters (if any)
    boolean oneFieldWritten = false;
    for (Map.Entry<String, MethodHandle> queryField : this.queryFields) {
      try {
        // get the field value, skip nulls
        Object fieldValue = queryField.getValue().invoke(request);
        if (fieldValue == null) {
          continue;
        }

        // change types of collections to comma seperated strings
        String stringifiedVal;
        if (fieldValue instanceof Collection<?>) {
          Collection<?> col = (Collection<?>) fieldValue;
          // skip empty collections
          if (col.isEmpty()) {
            continue;
          }

          // append all field values
          StringBuilder builder = new StringBuilder();
          for (Object o : col) {
            builder.append(o).append(",");
          }

          // remove the last trailing comma
          stringifiedVal = builder.substring(0, builder.length() - 1);
        } else {
          stringifiedVal = Objects.toString(fieldValue);
        }

        // append the query parameter
        buffer.append(oneFieldWritten ? "&" : "?").append(queryField.getKey()).append("=").append(stringifiedVal);
        oneFieldWritten = true;
      } catch (Throwable exception) {
        throw new IllegalStateException("Unable to use reflection on field " + queryField, exception);
      }
    }

    // convert the buffer to one query string
    return buffer.toString();
  }
}
