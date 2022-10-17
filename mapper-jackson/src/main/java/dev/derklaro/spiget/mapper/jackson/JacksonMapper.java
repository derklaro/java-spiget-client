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

package dev.derklaro.spiget.mapper.jackson;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import dev.derklaro.spiget.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;

public final class JacksonMapper implements JsonMapper {

  public static final JacksonMapper INSTANCE = new JacksonMapper();

  private final ObjectMapper objectMapper;

  private JacksonMapper() {
    this.objectMapper = new ObjectMapper();
    this.objectMapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
    this.objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
    this.objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    this.objectMapper.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
    this.objectMapper.setPropertyNamingStrategy(SpigetClientPropertyNamingStrategy.INSTANCE);
  }

  @Override
  public @NonNull <T> String encode(@NonNull T data) {
    try {
      return this.objectMapper.writeValueAsString(data);
    } catch (JsonProcessingException exception) {
      // this shouldn't happen
      throw new IllegalStateException(String.format(
        "An unexpected json encoding exception was caught for %s:", data),
        exception);
    }
  }

  @Override
  public <T> @NonNull T decode(@NonNull InputStream stream, @NonNull Type type) {
    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      JavaType resolvedType = this.objectMapper.getTypeFactory().constructType(type);
      return this.objectMapper.readValue(reader, resolvedType);
    } catch (IOException exception) {
      throw new IllegalStateException(String.format(
        "Cannot deserialize object of type %s from data stream:", type),
        exception);
    }
  }
}
