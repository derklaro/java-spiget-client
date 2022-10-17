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

package dev.derklaro.spiget.http.httpclient5;

import dev.derklaro.spiget.SpigetClientConfig;
import dev.derklaro.spiget.client.AbstractSpigetClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import lombok.NonNull;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicClassicHttpRequest;
import org.apache.hc.core5.util.Timeout;

public class HttpClient5SpigetSpigetClient extends AbstractSpigetClient {

  private final CloseableHttpClient client;

  public HttpClient5SpigetSpigetClient(@NonNull SpigetClientConfig clientConfig) {
    super(clientConfig);
    this.client = createClient(clientConfig);
  }

  private static @NonNull CloseableHttpClient createClient(@NonNull SpigetClientConfig clientConfig) {
    return HttpClients.custom()
      .setDefaultRequestConfig(RequestConfig.custom()
        .setRedirectsEnabled(true)
        .setConnectTimeout(Timeout.ofMilliseconds(clientConfig.connectTimeout().toMillis()))
        .setResponseTimeout(Timeout.ofMilliseconds(clientConfig.requestTimeout().toMillis()))
        .build())
      .disableConnectionState()
      .build();
  }

  @Override
  protected @NonNull CompletableFuture<InputStream> doSendRequest(
    String body,
    @NonNull String uri,
    @NonNull String contentType,
    @NonNull String requestMethod
  ) {
    ClassicHttpRequest request = new BasicClassicHttpRequest(requestMethod, URI.create(uri));
    // headers
    request.addHeader("Content-Type", contentType);
    request.addHeader("User-Agent", this.clientConfig.userAgent());
    // body
    if (body != null) {
      request.setEntity(new StringEntity(body, ContentType.parse(contentType)));
    }

    return CompletableFuture.supplyAsync(() -> {
      try {
        CloseableHttpResponse response = this.client.execute(request);
        // get the response content if the request was successful
        if (response.getCode() >= 200 && response.getCode() < 300 && response.getEntity() != null) {
          return response.getEntity().getContent();
        }
        // illegal state
        throw new IllegalStateException("Unexpected http response code: " + response.getCode());
      } catch (IOException exception) {
        // unchecked rethrow
        throw new CompletionException(exception);
      }
    });
  }
}
