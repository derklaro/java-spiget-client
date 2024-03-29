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

package dev.derklaro.spiget.request.webhook;

import dev.derklaro.spiget.Request;
import dev.derklaro.spiget.SpigetClient;
import dev.derklaro.spiget.annotation.RequestData;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import lombok.Data;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data(staticConstructor = "create")
@Accessors(fluent = true, chain = true)
@RequestData(uri = "webhook/events", method = "GET")
public final class WebhookEvents implements Request<Set<String>> {

  private final transient SpigetClient client;

  @Override
  public @NonNull CompletableFuture<Set<String>> exec() {
    return this.client.sendRequest(this);
  }
}
