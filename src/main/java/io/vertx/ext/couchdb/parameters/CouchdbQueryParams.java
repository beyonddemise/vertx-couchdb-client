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

/**
 * Standard query parameter for db operations
 */
public class CouchdbQueryParams implements QueryParameter {

  boolean descending = false;
  String endkey = null;
  String startkey = null;
  int limit = 0;
  int skip = 0;

  /**
   * @return the descending
   */
  public boolean isDescending() {
    return descending;
  }

  /**
   * @param descending the descending to set
   */
  public CouchdbQueryParams setDescending(boolean descending) {
    this.descending = descending;
    return this;
  }

  /**
   * @return the endkey
   */
  public String getEndkey() {
    return endkey;
  }

  /**
   * @param endkey the endkey to set
   */
  public CouchdbQueryParams setEndkey(String endkey) {
    this.endkey = endkey;
    return this;
  }

  /**
   * @return the startkey
   */
  public String getStartkey() {
    return startkey;
  }

  /**
   * @param startkey the startkey to set
   */
  public CouchdbQueryParams setStartkey(String startkey) {
    this.startkey = startkey;
    return this;
  }

  /**
   * @return the limit
   */
  public int getLimit() {
    return limit;
  }

  /**
   * @param limit the limit to set
   */
  public CouchdbQueryParams setLimit(int limit) {
    this.limit = limit;
    return this;
  }

  /**
   * @return the skip
   */
  public int getSkip() {
    return skip;
  }

  /**
   * @param skip the skip to set
   */
  public CouchdbQueryParams setSkip(int skip) {
    this.skip = skip;
    return this;
  }

  public String appendParams(final String source) {
    StringBuilder b = new StringBuilder(source);
    String separator = "&";

    b.append("?descending=");
    b.append(descending);

    if (limit > 0) {
      b.append(separator);
      b.append("limit=");
      b.append(limit);
    }

    if (skip > 0) {
      b.append(separator);
      b.append("skip=");
      b.append(skip);
    }

    if (startkey != null) {
      b.append(separator);
      b.append("startkey=%22");
      b.append(startkey);
      b.append("%22");
    }

    if (endkey != null) {
      b.append(separator);
      b.append("startkey=%22");
      b.append(endkey);
      b.append("%22");
    }


    return b.toString();
  }

}
