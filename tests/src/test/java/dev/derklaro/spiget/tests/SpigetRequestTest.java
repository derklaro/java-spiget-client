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

package dev.derklaro.spiget.tests;

import dev.derklaro.spiget.SpigetClient;
import dev.derklaro.spiget.SpigetClientConfig;
import dev.derklaro.spiget.data.Sort;
import dev.derklaro.spiget.http.httpclient5.HttpClient5SpigetClient;
import dev.derklaro.spiget.http.java11.Java11SpigetClient;
import dev.derklaro.spiget.http.java8.Java8SpigetClient;
import dev.derklaro.spiget.mapper.gson.GsonMapper;
import dev.derklaro.spiget.mapper.jackson.JacksonMapper;
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
import dev.derklaro.spiget.request.webhook.WebhookStatus;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.CompletionException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

final class SpigetRequestTest {

  static Stream<Arguments> clients() {
    return Stream.of(
      Arguments.of(new Java8SpigetClient(SpigetClientConfig.create(GsonMapper.INSTANCE))),
      Arguments.of(new Java11SpigetClient(SpigetClientConfig.create(GsonMapper.INSTANCE))),
      Arguments.of(new Java11SpigetClient(SpigetClientConfig.create(JacksonMapper.INSTANCE))),
      Arguments.of(new HttpClient5SpigetClient(SpigetClientConfig.create(JacksonMapper.INSTANCE))));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorDetails(SpigetClient client) {
    var result = AuthorDetails.create(client).id(1).exec().join();
    Assertions.assertEquals("md_5", result.name());
    Assertions.assertEquals("md__5", result.identities().get("twitter"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorList(SpigetClient client) {
    var result = AuthorList.create(client)
      .size(5)
      .page(7)
      .sort(Sort.of("name", Sort.Order.ASC))
      .fields(Set.of("name"))
      .exec()
      .join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertNull(first.icon());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorResources(SpigetClient client) {
    var result = AuthorResources.create(client).authorId(1).size(5).sort(Sort.of("name", Sort.Order.ASC)).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(1, first.author().id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorReviews(SpigetClient client) {
    var result = AuthorReviews.create(client).authorId(1).page(2).size(1).exec().join();
    Assertions.assertEquals(1, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(1, first.author().id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorSearch(SpigetClient client) {
    var result = AuthorSearch.create(client).query("md_5").size(1).exec().join();
    Assertions.assertEquals(1, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(1, first.id());
    Assertions.assertEquals("md_5", first.name());
    Assertions.assertEquals("md__5", first.identities().get("twitter"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testCategoryDetails(SpigetClient client) {
    var result = CategoryDetails.create(client).categoryId(2).exec().join();
    Assertions.assertEquals(2, result.id());
    Assertions.assertEquals("Bungee - Spigot", result.name());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testCategoryList(SpigetClient client) {
    var result = CategoryList.create(client).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var withIdTwo = result.stream().filter(category -> category.id() == 2).findFirst().orElse(null);
    Assertions.assertNotNull(withIdTwo);
    Assertions.assertEquals("Bungee - Spigot", withIdTwo.name());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testCategoryResources(SpigetClient client) {
    var result = CategoryResources.create(client).categoryId(2).page(5).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(2, first.category().id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testFreeResourceList(SpigetClient client) {
    var result = FreeResourceList.create(client).size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testLastResourceUpdate(SpigetClient client) {
    var result = LastResourceUpdate.create(client).resourceId(2).exec().join();
    Assertions.assertNotNull(result.description());
    Assertions.assertEquals(2, result.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testLatestResourceVersion(SpigetClient client) {
    var result = LatestResourceVersion.create(client).resourceId(2).exec().join();
    Assertions.assertNotNull(result.uuid());
    Assertions.assertEquals(2, result.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testNewResourceList(SpigetClient client) {
    var result = NewResourceList.create(client).size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testPremiumResourceList(SpigetClient client) {
    var result = PremiumResourceList.create(client).size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceAuthor(SpigetClient client) {
    var result = ResourceAuthor.create(client).resourceId(2).exec().join();
    Assertions.assertEquals(106, result.id());
    Assertions.assertEquals("LaxWasHere", result.name());
    Assertions.assertEquals("LaxWasHere", result.identities().get("yahoo"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceDetails(SpigetClient client) {
    var result = ResourceDetails.create(client).resourceId(2).exec().join();
    Assertions.assertEquals(2, result.id());
    Assertions.assertEquals(1, result.existenceStatus());
    Assertions.assertEquals(1364368440, result.releaseDate());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceDownload(SpigetClient client) throws IOException {
    var target = Path.of("build/download.jar");
    Files.deleteIfExists(target);

    try (var out = Files.newOutputStream(target);
      var in = ResourceDownload.create(client).resourceId(2).exec().join()) {
      in.transferTo(out);
    }

    Assertions.assertTrue(Files.exists(target));
    Assertions.assertTrue(Files.size(target) > 35000);

    Files.delete(target);
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceList(SpigetClient client) {
    var result = ResourceList.create(client).size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceReviews(SpigetClient client) {
    var result = ResourceReviews.create(client).resourceId(2).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(5, first.rating().average());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceSearch(SpigetClient client) {
    var result = ResourceSearch.create(client)
      .size(6)
      .query("CloudNet")
      .field("name")
      .fields(Set.of("name"))
      .exec()
      .join();
    Assertions.assertEquals(6, result.size());

    var first = result.iterator().next();
    Assertions.assertNull(first.sourceCodeLink());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceUpdates(SpigetClient client) {
    var result = ResourceUpdates.create(client).resourceId(2).size(1).exec().join();
    Assertions.assertEquals(1, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(17, first.id());
    Assertions.assertEquals(2, first.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceVersionDetails(SpigetClient client) {
    var result = ResourceVersion.create(client).resourceId(19254).versionId(429596).exec().join();
    Assertions.assertNotNull(result.uuid());
    Assertions.assertEquals(429596, result.id());
    Assertions.assertEquals(19254, result.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  @SuppressWarnings("resource")
  void testResourceVersionDownload(SpigetClient client) {
    var exception = Assertions.assertThrows(
      CompletionException.class,
      () -> ResourceVersionDownload.create(client).resourceId(3).versionId(352).exec().join());
    var message = exception.getMessage();

    // we're not able to download a version around the spigot proxy, we can only check if we get a response
    // Server returned HTTP response code: 503 for URL: https://spigotmc.org/resources/3/download?version=352
    Assertions.assertTrue(message.contains("response code"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceVersions(SpigetClient client) {
    var result = ResourceVersions.create(client).resourceId(2).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var withIdTwo = result.stream().filter(version -> version.id() == 2).findFirst().orElse(null);
    Assertions.assertNotNull(withIdTwo);
    Assertions.assertEquals(2, withIdTwo.resource());
    Assertions.assertEquals("1.0", withIdTwo.name());
    Assertions.assertEquals("00000003-c001-1bca-0000-0179a782b8fc", withIdTwo.uuid().toString());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceForVersions(SpigetClient client) {
    var result = VersionResourceList.create(client).version("1.16").size(5).page(2).method("all").exec().join();
    Assertions.assertEquals(5, result.match().size());
    Assertions.assertEquals("all", result.method());
    Assertions.assertTrue(result.check().contains("1.16"));

    var first = result.match().iterator().next();
    Assertions.assertTrue(first.testedVersions().contains("1.16"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testApiStatus(SpigetClient client) {
    var result = ApiStatus.create(client).exec().join();
    Assertions.assertNotNull(result.server());

    var stats = result.stats();
    Assertions.assertNotNull(stats);
    Assertions.assertTrue(stats.resourceUpdates() > 0);
    Assertions.assertTrue(stats.resourceVersions() > 0);
  }

  @ParameterizedTest
  @MethodSource("clients")
  @Disabled("Deletion of hooks is now possible currently, we don't want to spam hooks")
  void testWebhook(SpigetClient client) {
    var url = String.format("https://%s.de", UUID.randomUUID().toString().replace("-", ""));

    // create
    var result = RegisterWebhook.create(client).url(url).events(Set.of("resource-update")).exec().join();
    Assertions.assertNotNull(result.id());
    Assertions.assertNotNull(result.secret());

    // status
    var status = WebhookStatus.create(client).hookId(result.id()).exec().join();
    Assertions.assertTrue(status.status() >= 0);
    Assertions.assertTrue(status.failedConnections() >= 0);

    // delete
    DeleteWebhook.create(client).hookId(result.id()).secret(result.secret()).exec().join();

    // should throw exception now, but it doesn't for some reason? Might be a bug on the spiget site
    // Assertions.assertThrows(Exception.class, () -> client.webhookStatus().hookId(result.id()).exec().join());
  }
}
