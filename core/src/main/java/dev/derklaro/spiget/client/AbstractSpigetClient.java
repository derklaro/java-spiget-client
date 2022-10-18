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
import dev.derklaro.spiget.SpigetClient;
import dev.derklaro.spiget.SpigetClientConfig;
import dev.derklaro.spiget.annotation.ExcludeQuery;
import dev.derklaro.spiget.annotation.RequestData;
import dev.derklaro.spiget.annotation.SerializedName;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.text.MessageFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractSpigetClient implements SpigetClient {

  public static final String BASE_URL = "https://api.spiget.org/v2/";

  // method handles
  private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();
  private static final MethodType GENERIC_FIELD_GETTER_TYPE = MethodType.methodType(Object.class, Object.class);

  protected final SpigetClientConfig clientConfig;
  private final Map<Class<?>, RequestInfo> cachedInformation = new ConcurrentHashMap<>();

  protected AbstractSpigetClient(@NonNull SpigetClientConfig clientConfig) {
    this.clientConfig = clientConfig;
  }

  @Override
  public @NonNull <T> CompletableFuture<T> sendRequest(@NonNull Request<T> request, @NonNull Object... uriParams) {
    RequestInfo info = this.getOrCreateInfo(request);
    return this.doSendRequest(
      null,
      info.formatUri(request, uriParams),
      info.contentType(),
      info.requestMethod()
    ).thenApply(stream -> this.clientConfig.jsonMapper().decode(stream, info.responseType()));
  }

  @Override
  public @NonNull <T> CompletableFuture<T> sendRequestAsBody(
    @NonNull Request<T> request,
    @NonNull Object... uriParams
  ) {
    RequestInfo info = this.getOrCreateInfo(request);
    return this.doSendRequest(
      this.clientConfig.jsonMapper().encode(request),
      info.formatUri(request, uriParams),
      info.contentType(),
      info.requestMethod()
    ).thenApply(stream -> this.clientConfig.jsonMapper().decode(stream, info.responseType()));
  }

  @Override
  public @NonNull CompletableFuture<Void> sendRequestWithoutResponse(
    @NonNull Request<?> request,
    @NonNull Object... uriParams
  ) {
    return this.sendRequestRaw(request, uriParams).thenAccept(stream -> {
      try {
        stream.close();
      } catch (IOException exception) {
        // let the future complete exceptionally
        throw new CompletionException(exception);
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
    @Nullable String body,
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
      List<Map.Entry<String, MethodHandle>> queryFields = new ArrayList<>();
      for (Field field : clazz.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())
          && !Modifier.isTransient(field.getModifiers())
          && !field.isAnnotationPresent(ExcludeQuery.class)
        ) {
          try {
            // get the name of the query parameter
            SerializedName serializedNameData = field.getAnnotation(SerializedName.class);
            String serializedName = serializedNameData == null ? field.getName() : serializedNameData.value();

            // get a method handle for the field
            field.setAccessible(true);
            MethodHandle fieldGetter = LOOKUP.unreflectGetter(field);

            // convert the handle to a generic one
            MethodHandle fieldGetterGeneric = fieldGetter.asType(GENERIC_FIELD_GETTER_TYPE);
            queryFields.add(new AbstractMap.SimpleImmutableEntry<>(serializedName, fieldGetterGeneric));
          } catch (Exception exception) {
            // generic exception to catch InaccessibleObjectException as well
            throw new IllegalArgumentException(String.format(
              "Exception getting request information for request %s (field: %s):",
              clazz.getCanonicalName(), field
            ), exception);
          }
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
}
