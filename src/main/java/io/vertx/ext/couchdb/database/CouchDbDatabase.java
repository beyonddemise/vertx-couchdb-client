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
