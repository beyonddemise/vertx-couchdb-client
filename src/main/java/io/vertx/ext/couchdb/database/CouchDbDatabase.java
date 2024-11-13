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

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.impl.CouchDbDatabaseImpl;
import io.vertx.ext.couchdb.database.security.DBSecurity;
import io.vertx.ext.couchdb.parameters.DocumentGetParams;
import io.vertx.ext.couchdb.streams.CouchDbStream;

public interface CouchDbDatabase {

  static Future<CouchDbDatabase> create(CouchdbClient client, String databaseName) {
    return CouchDbDatabaseImpl.create(client, databaseName);
  }

  String name();

  Future<JsonObject> status();

  CouchDbStream stream(/* TODO: replace with specific object */ JsonObject options);

  Future<JsonObject> createDocument(String docId, JsonObject document);

  Future<JsonObject> updateDocument(String docId, String rev, JsonObject document);

  Future<JsonObject> getDocument(String docId, DocumentGetParams options);

  Future<JsonObject> getSecurity();

  Future<JsonObject> setSecurity(DBSecurity security);

  default Future<JsonObject> getDocument(String docId) {
    return this.getDocument(docId, null);
  };

  Future<Buffer> getDocumentAttachment(String docId, String attachementName,
      String rev);

  default Future<Buffer> getDocumentAttachment(String docId, String attachementName) {
    return this.getDocumentAttachment(docId, attachementName, null);
  }

  default Future<JsonObject> deleteDocument(String docId, String rev) {
    return this.deleteDocument(docId, rev, false);
  }

  Future<JsonObject> deleteDocument(String docId, String rev, boolean force);
}
