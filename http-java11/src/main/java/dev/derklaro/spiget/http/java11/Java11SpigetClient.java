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

package dev.derklaro.spiget.http.java11;

import dev.derklaro.spiget.SpigetClientConfig;
import dev.derklaro.spiget.client.AbstractSpigetClient;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;
import org.jetbrains.annotations.Nullable;

public final class Java11SpigetClient extends AbstractSpigetClient {

  private final HttpClient client;

  public Java11SpigetClient(@NonNull SpigetClientConfig clientConfig) {
    super(clientConfig);
    this.client = createClient(clientConfig);
  }

  private static @NonNull HttpClient createClient(@NonNull SpigetClientConfig clientConfig) {
    return HttpClient.newBuilder()
      .version(HttpClient.Version.HTTP_2)
      .connectTimeout(clientConfig.connectTimeout())
      .followRedirects(HttpClient.Redirect.NORMAL)
      .build();
  }

  @Override
  protected @NonNull CompletableFuture<InputStream> doSendRequest(
    @Nullable String body,
    @NonNull String uri,
    @NonNull String contentType,
    @NonNull String requestMethod
  ) {
    return this.client.sendAsync(
      HttpRequest.newBuilder(URI.create(uri))
        .timeout(this.clientConfig.requestTimeout())
        .header("Content-Type", contentType)
        .header("User-Agent", this.clientConfig.userAgent())
        .method(
          requestMethod,
          body != null ? HttpRequest.BodyPublishers.ofString(body) : HttpRequest.BodyPublishers.noBody())
        .build(),
      HttpResponse.BodyHandlers.ofInputStream()
    ).thenApply(response -> {
      // validate that the request was successful
      if (response.statusCode() >= 200 && response.statusCode() < 300) {
        return response.body();
      } else {
        // illegal state
        throw new IllegalStateException("Unexpected response code " + response.statusCode());
      }
    });
  }
}
