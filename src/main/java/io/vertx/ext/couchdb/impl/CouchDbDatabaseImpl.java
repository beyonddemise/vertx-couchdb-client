package io.vertx.ext.couchdb.impl;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.CouchDbDatabase;
import io.vertx.ext.couchdb.CouchDbStream;
import io.vertx.ext.web.client.WebClient;

public class CouchDbDatabaseImpl extends CouchdbClientImpl implements CouchDbDatabase {

  CouchDbDatabaseImpl(Vertx vertx, WebClient client, Credentials credentials) {
    super(vertx, client, credentials);
  }


  @Override
  public Future<JsonObject> createOrUpdateDesignDocument(String dbName, String designDocName, JsonObject designDocument) {
    JsonObject params = new JsonObject()
      .put("method", "PUT")
      .put("path", "/" + dbName + "/_design/" + designDocName)
      .put("body", designDocument);

    return rawCall(params).map(Buffer::toJsonObject);
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
