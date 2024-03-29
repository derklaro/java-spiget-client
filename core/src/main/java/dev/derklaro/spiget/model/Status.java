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

import dev.derklaro.spiget.annotation.SerializedName;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;

@Data
@Accessors(fluent = true)
public final class Status {

  // hide this wrapper as it's stupid
  @Getter(AccessLevel.NONE)
  private Wrapper status;
  private Stats stats;

  public @NonNull Server server() {
    return this.status.server();
  }

  @Data
  @Accessors(fluent = true)
  private static final class Wrapper {

    private Server server;
  }

  @Data
  @Accessors(fluent = true)
  public static final class Server {

    private String name;
    private String mode;
  }

  @Data
  @Accessors(fluent = true)
  public static final class Stats {

    private int resources;
    private int authors;
    private int categories;
    private int reviews;
    @SerializedName("resource_updates")
    private int resourceUpdates;
    @SerializedName("resource_versions")
    private int resourceVersions;
  }
}
