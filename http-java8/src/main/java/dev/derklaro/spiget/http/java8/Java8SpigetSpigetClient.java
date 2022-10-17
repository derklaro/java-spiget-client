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

package dev.derklaro.spiget.http.java8;

import dev.derklaro.spiget.JsonMapper;
import dev.derklaro.spiget.client.AbstractSpigetClient;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.NonNull;

public class Java8SpigetSpigetClient extends AbstractSpigetClient {

  public Java8SpigetSpigetClient(@NonNull JsonMapper mapper) {
    super(mapper);
  }

  @Override
  protected @NonNull CompletableFuture<InputStream> doSendRequest(
    String body,
    @NonNull String uri,
    @NonNull String contentType,
    @NonNull String requestMethod
  ) {
    return CompletableFuture.supplyAsync(() -> {
      try {
        HttpURLConnection connection = (HttpURLConnection) new URL(uri).openConnection();
        // boolean properties
        connection.setDoInput(true);
        connection.setUseCaches(false);
        connection.setDoOutput(body != null);
        connection.setAllowUserInteraction(false);
        connection.setInstanceFollowRedirects(true);
        // request method
        connection.setRequestMethod(requestMethod);
        // timeouts
        connection.setReadTimeout(30000);
        connection.setConnectTimeout(15000);
        // request properties
        connection.setRequestProperty("content-type", contentType);
        connection.setRequestProperty("User-Agent", "spiget-java-client");
        // connect and send the body if present
        connection.connect();
        if (body != null) {
          try (OutputStream out = connection.getOutputStream()) {
            out.write(body.getBytes(StandardCharsets.UTF_8));
            out.flush();
          }
        }
        // we can use the input stream here as we always require an "ok" response
        return connection.getInputStream();
      } catch (IOException exception) {
        throw new CompletionException(exception);
      }
    });
  }
}
