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
  id("com.github.ben-manes.versions") version "0.41.0"
  id("org.cadixdev.licenser") version "0.6.1" apply false
}

allprojects {
  group = "dev.derklaro"
  version = "1.0.0-SNAPSHOT"

  repositories {
    mavenCentral()
  }
}

subprojects {
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

  extensions.configure<LicenseExtension> {
    include("**/*.java")
    header(rootProject.file("license_header.txt"))
  }

  extensions.configure<CheckstyleExtension> {
    toolVersion = "9.2.1"
  }
}
