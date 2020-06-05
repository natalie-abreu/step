// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.data;

/** An item on a todo list. */
public final class Comment {

  public static String DATA_TYPE = "Comment";
  public static String TIMESTAMP = "timestamp";
  public static String AUTHOR = "name";
  public static String CONTENT = "message";

  private final long id;
  private final String name;
  private final long timestamp;
  private final String message;

  public Comment(long id, String name, long timestamp, String message) {
    this.id = id;
    this.name = name;
    this.timestamp = timestamp;
    this.message = message;
  }
}