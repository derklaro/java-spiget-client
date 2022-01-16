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

import dev.derklaro.spiget.JsonMapper;
import dev.derklaro.spiget.Request;
import dev.derklaro.spiget.SpigetClient;
import dev.derklaro.spiget.annotation.ExcludeQuery;
import dev.derklaro.spiget.annotation.RequestData;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;

@RequiredArgsConstructor
public abstract class AbstractClient extends SpigetClient {

  public static final String BASE_URL = "https://api.spiget.org/v2/";

  private final JsonMapper mapper;
  private final Map<Class<?>, RequestInfo> cachedInformation = new ConcurrentHashMap<>();

  @Override
  public @NonNull <T> CompletableFuture<T> sendRequest(@NonNull Request<T> request, @NonNull Object... uriParams) {
    RequestInfo info = this.getOrCreateInfo(request);
    return this.doSendRequest(
      null,
      info.formatUri(request, uriParams),
      info.contentType(),
      info.requestMethod()
    ).thenApply(stream -> this.mapper.decode(stream, info.responseType()));
  }

  @Override
  public @NonNull <T> CompletableFuture<T> sendRequestInBody(
    @NonNull Request<T> request,
    @NonNull Object... uriParams
  ) {
    RequestInfo info = this.getOrCreateInfo(request);
    return this.doSendRequest(
      this.mapper.encode(request),
      info.formatUri(request, uriParams),
      info.contentType(),
      info.requestMethod()
    ).thenApply(stream -> this.mapper.decode(stream, info.responseType()));
  }

  @Override
  public @NonNull CompletableFuture<Void> sendRequestEmpty(@NonNull Request<?> request, @NonNull Object... uriParams) {
    return this.sendRequestRaw(request, uriParams).thenAccept(stream -> {
      try {
        stream.close();
      } catch (IOException exception) {
        // let the future complete exceptionally
        throw new UncheckedIOException(exception);
      }
    });
  }

  @Override
  public @NonNull CompletableFuture<InputStream> sendRequestRaw(
    @NonNull Request<?> request,
    @NonNull Object... uriParams
  ) {
    RequestInfo info = this.getOrCreateInfo(request);
    return this.doSendRequest(null, info.formatUri(request, uriParams), info.contentType(), info.requestMethod());
  }

  @NonNull
  protected abstract CompletableFuture<InputStream> doSendRequest(
    String body,
    @NonNull String uri,
    @NonNull String contentType,
    @NonNull String requestMethod);

  protected @NonNull RequestInfo getOrCreateInfo(@NonNull Request<?> request) {
    return this.cachedInformation.computeIfAbsent(request.getClass(), clazz -> {
      // get the annotation
      RequestData data = clazz.getDeclaredAnnotation(RequestData.class);
      Objects.requireNonNull(data, "no request annotation is present");
      // get the response type information
      Type responseType = null;
      for (Type type : clazz.getGenericInterfaces()) {
        // must be a parameterized type (Response<ResponseType>)
        if (type instanceof ParameterizedType) {
          // validate that the type is actually the request and has a type parameter
          ParameterizedType parameterized = (ParameterizedType) type;
          if (parameterized.getRawType().equals(Request.class) && parameterized.getActualTypeArguments().length == 1) {
            responseType = parameterized.getActualTypeArguments()[0];
            break;
          }
        }
      }
      // check if the type was given
      if (responseType == null) {
        throw new IllegalArgumentException("Missing type parameter.");
      }
      // get the fields which are included in the query
      List<Field> queryFields = new ArrayList<>();
      for (Field field : clazz.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isTransient(field.getModifiers())
          && !field.isAnnotationPresent(ExcludeQuery.class)
        ) {
          field.setAccessible(true);
          queryFields.add(field);
        }
      }
      // build the info
      return new RequestInfo(
        responseType,
        data.contentType(),
        new MessageFormat(BASE_URL + data.uri()),
        data.method(),
        queryFields);
    });
  }

  @Data
  @Accessors(fluent = true)
  protected static final class RequestInfo {

    private static final Object[] EMPTY = new Object[0];

    private final Type responseType;
    private final String contentType;
    private final MessageFormat format;
    private final String requestMethod;
    private final List<Field> queryFields;

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
      for (Field queryField : this.queryFields) {
        try {
          // get the field value, skip nulls
          Object fieldValue = queryField.get(request);
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
          buffer.append(oneFieldWritten ? "&" : "?").append(queryField.getName()).append("=").append(stringifiedVal);
          oneFieldWritten = true;
        } catch (ReflectiveOperationException exception) {
          throw new IllegalStateException("Unable to use reflection on field " + queryField, exception);
        }
      }
      // convert the buffer to one query string
      return buffer.toString();
    }
  }
}
