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
import dev.derklaro.spiget.data.Sort;
import dev.derklaro.spiget.data.Sort.Order;
import dev.derklaro.spiget.http.httpclient5.HttpClient5SpigetClient;
import dev.derklaro.spiget.http.java11.Java11SpigetClient;
import dev.derklaro.spiget.http.java8.Java8SpigetClient;
import dev.derklaro.spiget.mapper.gson.GsonMapper;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@TestMethodOrder(OrderAnnotation.class)
public class SpigetRequestTest {

  static Stream<Arguments> clients() {
    return Stream.of(
      Arguments.of(new Java8SpigetClient(GsonMapper.INSTANCE)),
      Arguments.of(new Java11SpigetClient(GsonMapper.INSTANCE)),
      Arguments.of(new HttpClient5SpigetClient(GsonMapper.INSTANCE)));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorList(SpigetClient client) {
    var result = client.authorList().size(5).page(7).sort(Sort.of("name", Order.ASC)).fields(Set.of("name")).exec()
      .join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertNull(first.icon());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorDetails(SpigetClient client) {
    var result = client.authorDetails().id(1).exec().join();
    Assertions.assertEquals("md_5", result.name());
    Assertions.assertEquals("md__5", result.identities().get("twitter"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorResources(SpigetClient client) {
    var result = client.authorResources().authorId(1).size(5).sort(Sort.of("name", Order.ASC)).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(1, first.author().id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorReviews(SpigetClient client) {
    var result = client.authorReviews().authorId(1).page(2).size(1).exec().join();
    Assertions.assertEquals(1, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(1, first.author().id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testAuthorSearch(SpigetClient client) {
    var result = client.authorSearch().query("md_5").size(1).exec().join();
    Assertions.assertEquals(1, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(1, first.id());
    Assertions.assertEquals("md_5", first.name());
    Assertions.assertEquals("md__5", first.identities().get("twitter"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testCategoryList(SpigetClient client) {
    var result = client.categoryList().size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(2, first.id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testCategoryDetails(SpigetClient client) {
    var result = client.categoryDetails().categoryId(2).exec().join();
    Assertions.assertEquals(2, result.id());
    Assertions.assertEquals("Bungee - Spigot", result.name());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testCategoryResources(SpigetClient client) {
    var result = client.categoryResources().categoryId(2).page(5).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(2, first.category().id());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceList(SpigetClient client) {
    var result = client.resourceList().size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceForVersions(SpigetClient client) {
    var result = client.versionResourceList().version("1.16").size(5).page(2).method("all").exec().join();
    Assertions.assertEquals(5, result.match().size());
    Assertions.assertEquals("all", result.method());
    Assertions.assertTrue(result.check().contains("1.16"));

    var first = result.match().iterator().next();
    Assertions.assertTrue(first.testedVersions().contains("1.16"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testFreeResourceList(SpigetClient client) {
    var result = client.freeResourceList().size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testNewResourceList(SpigetClient client) {
    var result = client.newResourceList().size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testPremiumResourceList(SpigetClient client) {
    var result = client.premiumResourceList().size(5).page(3).exec().join();
    Assertions.assertEquals(5, result.size());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceDetails(SpigetClient client) {
    var result = client.resourceDetails().resourceId(2).exec().join();
    Assertions.assertEquals(2, result.id());
    Assertions.assertEquals(1, result.existenceStatus());
    Assertions.assertEquals(1364368440, result.releaseDate());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceAuthor(SpigetClient client) {
    var result = client.resourceAuthor().resourceId(2).exec().join();
    Assertions.assertEquals(106, result.id());
    Assertions.assertEquals("LaxWasHere", result.name());
    Assertions.assertEquals("LaxWasHere", result.identities().get("yahoo"));
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceDownload(SpigetClient client) throws IOException {
    var target = Path.of("build/download.jar");
    Files.deleteIfExists(target);

    try (var out = Files.newOutputStream(target);
      var in = client.resourceDownload().resourceId(2).exec().join()) {
      in.transferTo(out);
    }

    Assertions.assertTrue(Files.exists(target));
    Assertions.assertTrue(Files.size(target) > 35000);

    Files.delete(target);
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceReviews(SpigetClient client) {
    var result = client.resourceReviews().resourceId(2).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(5, first.rating().average());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceUpdates(SpigetClient client) {
    var result = client.resourceUpdates().resourceId(2).size(1).exec().join();
    Assertions.assertEquals(1, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(17, first.id());
    Assertions.assertEquals(2, first.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testLastResourceUpdate(SpigetClient client) {
    var result = client.lastResourceUpdate().resourceId(2).exec().join();
    Assertions.assertNotNull(result.description());
    Assertions.assertEquals(2, result.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceVersions(SpigetClient client) {
    var result = client.resourceVersions().resourceId(2).size(5).exec().join();
    Assertions.assertEquals(5, result.size());

    var first = result.iterator().next();
    Assertions.assertEquals(2, first.id());
    Assertions.assertEquals(2, first.resource());
    Assertions.assertNotNull(first.uuid());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testLatestResourceVersion(SpigetClient client) {
    var result = client.latestResourceVersion().resourceId(2).exec().join();
    Assertions.assertNotNull(result.uuid());
    Assertions.assertEquals(2, result.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceVersionDetails(SpigetClient client) {
    var result = client.resourceVersion().resourceId(19254).versionId(429596).exec().join();
    Assertions.assertNotNull(result.uuid());
    Assertions.assertEquals(429596, result.id());
    Assertions.assertEquals(19254, result.resource());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testResourceSearch(SpigetClient client) {
    var result = client.resourceSearch().size(6).query("CloudNet").field("name").fields(Set.of("name")).exec().join();
    Assertions.assertEquals(6, result.size());

    var first = result.iterator().next();
    Assertions.assertNull(first.sourceCodeLink());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testApiStatus(SpigetClient client) {
    var result = client.apiStatus().exec().join();
    Assertions.assertNotNull(result.stats());
    Assertions.assertNotNull(result.server());
  }

  @ParameterizedTest
  @MethodSource("clients")
  void testWebhook(SpigetClient client) {
    var url = String.format("https://%s.de", UUID.randomUUID().toString().replace("-", ""));

    // create
    var result = client.registerWebhook().url(url).events(Set.of("resource-update")).exec().join();
    Assertions.assertNotNull(result.id());
    Assertions.assertNotNull(result.secret());

    // status
    var status = client.webhookStatus().hookId(result.id()).exec().join();
    Assertions.assertTrue(status.status() >= 0);
    Assertions.assertTrue(status.failedConnections() >= 0);

    // delete
    client.deleteWebhook().hookId(result.id()).secret(result.secret()).exec().join();

    // should throw exception now, but it doesn't for some reason? Might be a bug on the spiget site
    // Assertions.assertThrows(Exception.class, () -> client.webhookStatus().hookId(result.id()).exec().join());
  }
}
