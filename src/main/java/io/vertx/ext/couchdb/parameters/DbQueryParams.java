/*
 * Copyright (c) 2024 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.ext.couchdb.parameters;

import java.util.Arrays;
import java.util.List;

/**
 * Standard query parameter for db operations _all_dbs & _dbs_info
 */
public class DbQueryParams extends BaseQueryParameters {

  static final List<String> keys =
      Arrays.asList("descending", "endkey", "startkey", "limit", "skip");

  /**
   * @param descending the descending to set
   */
  public DbQueryParams descending(boolean descending) {
    this.paramStore.put("descending", descending);
    return this;
  }

  /**
   * @param endkey the endkey to set
   */
  public DbQueryParams endkey(String endkey) {
    String key = urlEncoded(String.format("\"%s\"", endkey));
    this.paramStore.put("endkey", key);
    return this;
  }


  /**
   * @param startkey the startkey to set
   */
  public DbQueryParams startkey(String startkey) {
    String key = urlEncoded(String.format("\"%s\"", startkey));
    this.paramStore.put("startkey", key);
    return this;
  }


  /**
   * @param limit the limit to set
   */
  public DbQueryParams limit(int limit) {
    this.paramStore.put("limit", limit);
    return this;
  }


  /**
   * @param skip the skip to set
   */
  public DbQueryParams skip(int skip) {
    this.paramStore.put("skip", skip);
    return this;
  }

}
