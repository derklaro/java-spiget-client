# Spiget Java Client

![Build](https://github.com/derklaro/java-spiget-client/actions/workflows/build.yml/badge.svg)
![MIT License](https://img.shields.io/badge/license-MIT-blue)
![Release Version](https://img.shields.io/maven-central/v/dev.derklaro.spiget/core)

### Dependencies

The spiget java client is available in the maven central repository.

```xml

<dependencies>
  <dependency>
    <groupId>dev.derklaro.spiget</groupId>
    <artifactId>{component}</artifactId>
    <version>{release}</version>
  </dependency>
</dependencies>
```

```gradle
implementation group: 'dev.derklaro.spiget', name: '{component}', version: '{version}'
```

### Dev Builds

Dev Builds are published to sonatype, use the following repository to access them:

```xml

<repositories>
  <repository>
    <id>sonatype-snapshots</id>
    <url>https://s01.oss.sonatype.org/content/repositories/snapshots/</url>
  </repository>
</repositories>
```

### Components

The library has 5 main components, 3 are required to run it. You need the core to get access to all request and response
types, one http implementation to send a request and one json-mapper to en- / decode the request/response data:

- `core`: contains all request and response models, the main api to use
- `http-java8`: contains the java 8 (`HttpUrlConnection`) based http client implementation.
- `http-java11`: contains the java 11 (`HttpClient`) based http client implementation
- `http-httpclient5`: contains the apache client5 based http implementation
- `mapper-gson`: contains a json mapper based on gson to en- / decode data
- `mapper-jackson`: contains a json mapper based on jackson to en- / decode data
- `tests`: contains all tests, no use for a user

### External dependencies

Please note that no component shadows any dependency by default. Your library is required to define the following 
dependencies if you use the given component:

| Component        | Required dependencies                           | Required Version Minimum |
|------------------|-------------------------------------------------|--------------------------|
| core             | no dependencies required                        | Java >= 8                |
| http-java8       | no dependencies required                        | Java >= 8                |
| http-java11      | no dependencies required                        | Java >= 11               |
| http-httpclient5 | `org.apache.httpcomponents.client5:httpclient5` | 5.X                      |
| mapper-gson      | `com.google.code.gson:gson`                     | 2.X                      |

### How to send a request

*This example uses the Java 11 client and the gson mapper, but it's the same thing for all other clients/mappers.*

A request will always be sent to the spiget base url `https://api.spiget.org/v2/`. Each request (and the possible fields
of it) described in the spiget documentation (https://spiget.org/documentation) has a wrapper representation:

```java
package dev.derklaro.spiget.example;

import dev.derklaro.spiget.SpigetClient;
import dev.derklaro.spiget.SpigetClientConfig;
import dev.derklaro.spiget.http.java11.Java11SpigetClient;
import dev.derklaro.spiget.mapper.gson.GsonMapper;
import dev.derklaro.spiget.model.Category;
import dev.derklaro.spiget.request.category.CategoryList;
import dev.derklaro.spiget.request.resource.ResourceDownload;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public final class SpigetRequester {

  private final SpigetClient client;

  public SpigetRequester() {
    // creates a new spiget client which uses the gson mapper and the connect timeout is set to 30 seconds.
    // You can configure the request timeout and the user agent as well.
    SpigetClientConfig config = SpigetClientConfig.create(GsonMapper.INSTANCE).connectTimeout(Duration.ofSeconds(30));
    this.client = new Java11SpigetClient(config);
  }

  public Set<Category> listCategories() {
    // sends the request and waits for the result to become available
    // this will rethrow any exception if one occurs
    return CategoryList.create(this.client).size(5).exec().join();
  }

  public void listAndPrintCategories() {
    // recommended way to use the api
    // make use of the method given by CompletableFuture
    CategoryList.create(this.client).size(5).exec().whenComplete((result, exception) -> {
      if (exception != null) {
        System.err.println("Unable to fetch because " + exception.getMessage());
      } else {
        result.forEach(category -> System.out.println(category.name()));
      }
    });
  }

  public CompletableFuture<Void> downloadFile() {
    // a file download requires you to close the stream
    // any other request which returns a model will do this for you
    return ResourceDownload.create(this.client).resourceId(2).exec().thenAccept(stream -> {
      try (InputStream inputStream = stream) {
        // copies the stream to the given file path
        Files.copy(inputStream, Paths.get("target.jar"));
      } catch (IOException exception) {
        // let the caller of the method deal with the exception
        throw new UncheckedIOException(exception);
      }
    });
  }
}
```

### Compiling from source

Just executing `./gradlew` or `gradlew.bat` will execute the full build lifecycle including all tests. For local changes
and testing use `./gradlew publishToMavenLocal` to publish all artifacts into the local maven repository.

First time example:

```
git clone https://github.com/derklaro/java-spiget-client.git
cd java-spiget-client/
./gradlew
```

### Contributions

Open source lives from contributions so feel free to contribute! Before opening a pull request, please make sure that
all tests are still passing and checkstyle prints no warnings during compile. Please include a test case if necessary.

Breaking changes are only accepted if they are caused by spiget (e.g. spiget removed a query field).

### License

This project is licensed under the terms of the [MIT License](../license.txt).
