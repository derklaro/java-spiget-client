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

package dev.derklaro.spiget.mapper.gson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.derklaro.spiget.JsonMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import lombok.NonNull;

public final class GsonMapper implements JsonMapper {

  public static final GsonMapper INSTANCE = new GsonMapper();

  private final Gson gson = new GsonBuilder().disableHtmlEscaping().create();

  private GsonMapper() {
  }

  @Override
  public @NonNull <T> String encode(@NonNull T data) {
    return this.gson.toJson(data);
  }

  @Override
  public <T> @NonNull T decode(@NonNull InputStream stream, @NonNull Type type) {
    try (Reader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
      return this.gson.fromJson(reader, type);
    } catch (IOException exception) {
      throw new UncheckedIOException(exception);
    }
  }
}
