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

package io.vertx.ext.couchdb.streams.impl;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.streams.CouchDbStream;

@VertxGen
public class CouchDbStreamImpl implements CouchDbStream {

  private CouchdbClient client;
  private final String database;
  private final int batchSize;
  private String lastDocId;
  private boolean paused;
  private Handler<JsonObject> handler;
  private Handler<Void> endHandler;
  private Handler<Throwable> exceptionHandler;

  public CouchDbStreamImpl(CouchdbClient client, String database, int batchSize) {
    this.client = client;
    this.database = database;
    this.batchSize = batchSize;
    this.lastDocId = null;
    this.paused = false;
  }

  @Override
  public CouchDbStream exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  @Override
  public CouchDbStream handler(Handler<JsonObject> handler) {
    this.handler = handler;
    if (handler != null && !paused) {
      doFetch();
    }
    return this;
  }

  @Override
  public CouchDbStream pause() {
    paused = true;
    return this;
  }

  @Override
  public CouchDbStream resume() {
    if (paused) {
      paused = false;
      doFetch();
    }
    return this;
  }

  @Override
  public CouchDbStream fetch(long amount) {
    return this;
  }

  @Override
  public CouchDbStream endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  private void doFetch() {
    if (handler == null || paused) {
      return;
    }

    JsonObject queryParams = new JsonObject()
        .put("limit", batchSize + 1)
        .put("include_docs", true);

    if (lastDocId != null) {
      queryParams.put("startkey", "\"" + lastDocId + "\"")
          .put("skip", 1);
    }

    // input logic for fetch
  }
}
