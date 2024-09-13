package io.vertx.ext.couchdb.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.CouchDbDatabase;
import io.vertx.ext.couchdb.CouchDbStream;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.web.client.WebClient;

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

    System.out.println("Params: " + params);

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
