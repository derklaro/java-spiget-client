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

package dev.derklaro.spiget.model;

import java.util.Map;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class Resource {

  private final int id;
  private final String name;

  private final String tag;
  private final String contributors;

  private final int likes;
  private final FileInfo file;

  private final Set<String> testedVersions;
  private final Map<String, String> links;

  private final Rating rating;
  private final AuthorId author;
  private final CategoryId category;

  private final long releaseDate;
  private final long updatedDate;
  private final int downloads;

  private final boolean external;
  private final Icon icon;

  private final boolean premium;
  private final int price;
  private final String currency;

  private final String sourceCodeLink;
  private final String donationLink;

  private final int existenceStatus;
  private final String supportedLanguages;

  private final Version version;
  private final Set<Version> versions;

  private final Set<UpdateId> updates;
  private final Set<ReviewId> reviews;

  @Data
  @Accessors(fluent = true)
  public static final class Version {

    private final int id;
    private final UUID uuid;
  }

  @Data
  @Accessors(fluent = true)
  public static final class UpdateId {

    private final int id;
  }

  @Data
  @Accessors(fluent = true)
  public static final class ReviewId {

    private final int id;
  }

  @Data
  @Accessors(fluent = true)
  public static final class AuthorId {

    private final int id;
  }

  @Data
  @Accessors(fluent = true)
  public static final class CategoryId {

    private final int id;
  }
}
