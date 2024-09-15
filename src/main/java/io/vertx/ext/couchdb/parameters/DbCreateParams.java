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
public class DbCreateParams extends BaseQueryParameters {

  static final List<String> keys =
      Arrays.asList("q", "n", "partitioned");

  @Override
  public List<String> knownParameters() {
    return DbCreateParams.keys;
  }

  /**
   * @param descending the descending to set
   */
  public DbCreateParams partitioned(boolean partitioned) {
    this.paramStore.put("partitioned", partitioned);
    return this;
  }

  /**
   * @param endkey the endkey to set
   */
  public DbCreateParams shards(int shards) {
    this.paramStore.put("q", shards);
    return this;
  }


  /**
   * @param startkey the startkey to set
   */
  public DbCreateParams replicas(int replicas) {
    this.paramStore.put("n", replicas);
    return this;
  }

}
