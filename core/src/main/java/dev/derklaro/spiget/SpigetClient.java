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

package dev.derklaro.spiget;

import dev.derklaro.spiget.request.author.AuthorDetails;
import dev.derklaro.spiget.request.author.AuthorList;
import dev.derklaro.spiget.request.author.AuthorResources;
import dev.derklaro.spiget.request.author.AuthorReviews;
import dev.derklaro.spiget.request.author.AuthorSearch;
import dev.derklaro.spiget.request.category.CategoryDetails;
import dev.derklaro.spiget.request.category.CategoryList;
import dev.derklaro.spiget.request.category.CategoryResources;
import dev.derklaro.spiget.request.resource.FreeResourceList;
import dev.derklaro.spiget.request.resource.LastResourceUpdate;
import dev.derklaro.spiget.request.resource.LatestResourceVersion;
import dev.derklaro.spiget.request.resource.NewResourceList;
import dev.derklaro.spiget.request.resource.PremiumResourceList;
import dev.derklaro.spiget.request.resource.ResourceAuthor;
import dev.derklaro.spiget.request.resource.ResourceDetails;
import dev.derklaro.spiget.request.resource.ResourceDownload;
import dev.derklaro.spiget.request.resource.ResourceList;
import dev.derklaro.spiget.request.resource.ResourceReviews;
import dev.derklaro.spiget.request.resource.ResourceSearch;
import dev.derklaro.spiget.request.resource.ResourceUpdates;
import dev.derklaro.spiget.request.resource.ResourceVersion;
import dev.derklaro.spiget.request.resource.ResourceVersionDownload;
import dev.derklaro.spiget.request.resource.ResourceVersions;
import dev.derklaro.spiget.request.resource.VersionResourceList;
import dev.derklaro.spiget.request.status.ApiStatus;
import dev.derklaro.spiget.request.webhook.DeleteWebhook;
import dev.derklaro.spiget.request.webhook.RegisterWebhook;
import dev.derklaro.spiget.request.webhook.WebhookEvents;
import dev.derklaro.spiget.request.webhook.WebhookStatus;
import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import lombok.NonNull;

public abstract class SpigetClient {

  public @NonNull AuthorDetails authorDetails() {
    return new AuthorDetails(this);
  }

  public @NonNull DeleteWebhook deleteWebhook() {
    return new DeleteWebhook(this);
  }

  public @NonNull AuthorSearch authorSearch() {
    return new AuthorSearch(this);
  }

  public @NonNull ResourceSearch resourceSearch() {
    return new ResourceSearch(this);
  }

  public @NonNull CategoryDetails categoryDetails() {
    return new CategoryDetails(this);
  }

  public @NonNull ResourceList resourceList() {
    return new ResourceList(this);
  }

  public @NonNull ApiStatus apiStatus() {
    return new ApiStatus(this);
  }

  public @NonNull CategoryResources categoryResources() {
    return new CategoryResources(this);
  }

  public @NonNull AuthorResources authorResources() {
    return new AuthorResources(this);
  }

  public @NonNull ResourceDownload resourceDownload() {
    return new ResourceDownload(this);
  }

  public @NonNull LatestResourceVersion latestResourceVersion() {
    return new LatestResourceVersion(this);
  }

  public @NonNull ResourceReviews resourceReviews() {
    return new ResourceReviews(this);
  }

  public @NonNull FreeResourceList freeResourceList() {
    return new FreeResourceList(this);
  }

  public @NonNull NewResourceList newResourceList() {
    return new NewResourceList(this);
  }

  public @NonNull ResourceVersion resourceVersion() {
    return new ResourceVersion(this);
  }

  public @NonNull ResourceVersionDownload resourceVersionDownload() {
    return new ResourceVersionDownload(this);
  }

  public @NonNull CategoryList categoryList() {
    return new CategoryList(this);
  }

  public @NonNull ResourceDetails resourceDetails() {
    return new ResourceDetails(this);
  }

  public @NonNull WebhookEvents webhookEvents() {
    return new WebhookEvents(this);
  }

  public @NonNull VersionResourceList versionResourceList() {
    return new VersionResourceList(this);
  }

  public @NonNull WebhookStatus webhookStatus() {
    return new WebhookStatus(this);
  }

  public @NonNull LastResourceUpdate lastResourceUpdate() {
    return new LastResourceUpdate(this);
  }

  public @NonNull AuthorList authorList() {
    return new AuthorList(this);
  }

  public @NonNull PremiumResourceList premiumResourceList() {
    return new PremiumResourceList(this);
  }

  public @NonNull ResourceVersions resourceVersions() {
    return new ResourceVersions(this);
  }

  public @NonNull AuthorReviews authorReviews() {
    return new AuthorReviews(this);
  }

  public @NonNull ResourceAuthor resourceAuthor() {
    return new ResourceAuthor(this);
  }

  public @NonNull RegisterWebhook registerWebhook() {
    return new RegisterWebhook(this);
  }

  public @NonNull ResourceUpdates resourceUpdates() {
    return new ResourceUpdates(this);
  }

  @NonNull
  public abstract <T> CompletableFuture<T> sendRequest(@NonNull Request<T> request, @NonNull Object... uriParams);

  @NonNull
  public abstract <T> CompletableFuture<T> sendRequestInBody(@NonNull Request<T> request, @NonNull Object... uriParams);

  @NonNull
  public abstract CompletableFuture<Void> sendRequestEmpty(@NonNull Request<?> request, @NonNull Object... uriParams);

  @NonNull
  public abstract CompletableFuture<InputStream> sendRequestRaw(@NonNull Request<?> request,
    @NonNull Object... uriParams);
}
