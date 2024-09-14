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
package io.vertx.ext.couchdb.database;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchDbStream;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.impl.CouchDbDatabaseImpl;

@VertxGen
public interface CouchDbDatabase {

  static CouchDbDatabase create(CouchdbClient client, String databaseName) {
    return CouchDbDatabaseImpl.create(client, databaseName);
  }

  Future<JsonObject> status();

  CouchDbStream stream(JsonObject options);

  Future<JsonObject> createOrUpdateDocument(String docId, JsonObject document);

  Future<JsonObject> getDocument(String docId, JsonObject options);
}
