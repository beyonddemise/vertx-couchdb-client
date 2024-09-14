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
package io.vertx.ext.couchdb.database.impl;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.couchdb.CouchDbStream;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.CouchDbDatabase;

public class CouchDbDatabaseImpl implements CouchDbDatabase {

  private final CouchdbClient client;

  private final String databaseName;

  CouchDbDatabaseImpl(CouchdbClient client, String databaseName) {
    this.client = client;
    this.databaseName = databaseName;
  }

  public static CouchDbDatabase create(CouchdbClient client, String databaseName) {
    return new CouchDbDatabaseImpl(client, databaseName);
  }

  @Override
  public Future<JsonObject> createOrUpdateDocument(String docId, JsonObject document) {
    JsonObject params = new JsonObject()
        .put("method", "PUT")
        .put("path", "/" + databaseName + "/" + docId)
        .put("body", document);

    if (document.containsKey("_rev")) {
      params.put("query", new JsonObject().put("rev", document.getString("_rev")));
    }

    return client.rawCall(params).map(Buffer::toJsonObject);
  }

  @Override
  public Future<JsonObject> getDocument(String docId, JsonObject options) {
    JsonObject params = new JsonObject()
        .put("method", "GET")
        .put("path", "/" + databaseName + "/" + docId)
        .put("query", options);

    return client.rawCall(params).map(Buffer::toJsonObject);
  }



  @Override
  public Future<JsonObject> status() {
    throw new UnsupportedOperationException("Unimplemented method 'status'");
  }

  @Override
  public CouchDbStream stream(JsonObject options) {
    throw new UnsupportedOperationException("Unimplemented method 'stream'");
  }
}
