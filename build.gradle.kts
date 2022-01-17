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

import org.cadixdev.gradle.licenser.LicenseExtension

plugins {
  id("org.cadixdev.licenser") version "0.6.1"
  id("com.github.ben-manes.versions") version "0.41.0"
  id("io.github.gradle-nexus.publish-plugin") version "1.1.0"
}

defaultTasks("build", "checkLicenses", "test", "jar")

allprojects {
  group = "dev.derklaro.spiget"
  version = "1.0.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "signing")
  apply(plugin = "checkstyle")
  apply(plugin = "java-library")
  apply(plugin = "maven-publish")
  apply(plugin = "org.cadixdev.licenser")

  dependencies {
    "compileOnly"(rootProject.libs.lombok)
    "annotationProcessor"(rootProject.libs.lombok)
  }

  tasks.withType<JavaCompile> {
    sourceCompatibility = JavaVersion.VERSION_1_8.toString()
    targetCompatibility = JavaVersion.VERSION_1_8.toString()
    // options
    options.encoding = "UTF-8"
    options.isIncremental = true
  }

  tasks.withType<Jar> {
    from(rootProject.file("license.txt"))
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
  }

  tasks.withType<Test> {
    useJUnitPlatform()
    testLogging {
      events("started", "passed", "skipped", "failed")
    }
  }

  tasks.withType<Checkstyle> {
    maxErrors = 0
    maxWarnings = 0
    configFile = rootProject.file("checkstyle.xml")
  }

  tasks.withType<Sign> {
    onlyIf {
      !project.rootProject.version.toString().endsWith("-SNAPSHOT")
    }
  }

  extensions.configure<LicenseExtension> {
    include("**/*.java")
    header(rootProject.file("license_header.txt"))
  }

  extensions.configure<JavaPluginExtension> {
    withSourcesJar()
    withJavadocJar()
  }

  extensions.configure<CheckstyleExtension> {
    toolVersion = "9.2.1"
  }

  extensions.configure<PublishingExtension> {
    publications {
      create("library", MavenPublication::class.java) {
        from(project.components.getByName("java"))

        pom {
          name.set(project.name)
          url.set("https://github.com/derklaro/java-spiget-client")

          licenses {
            license {
              name.set("MIT License")
              url.set("https://opensource.org/licenses/MIT")
            }
          }

          developers {
            developer {
              name.set("Pasqual Koschmieder")
              email.set("git@derklaro.dev")
            }
          }

          scm {
            url.set("https://github.com/derklaro/java-spiget-client")
            connection.set("https://github.com/derklaro/java-spiget-client.git")
          }

          issueManagement {
            system.set("GitHub Issues")
            url.set("https://github.com/derklaro/java-spiget-client/issues")
          }

          withXml {
            val repositories = asNode().appendNode("repositories")
            project.repositories.forEach {
              if (it is MavenArtifactRepository && it.url.toString().startsWith("https://")) {
                val repo = repositories.appendNode("repository")
                repo.appendNode("id", it.name)
                repo.appendNode("url", it.url.toString())
              }
            }
          }
        }
      }
    }
  }

  extensions.configure<SigningExtension> {
    useGpgCmd()
    sign(extensions.getByType(PublishingExtension::class.java).publications["library"])
  }
}

nexusPublishing {
  repositories {
    sonatype {
      nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
      snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))

      username.set(project.findProperty("ossrhUsername")?.toString() ?: "")
      password.set(project.findProperty("ossrhPassword")?.toString() ?: "")
    }
  }

  useStaging.set(!project.rootProject.version.toString().endsWith("-SNAPSHOT"))
}
