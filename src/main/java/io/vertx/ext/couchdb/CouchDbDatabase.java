package io.vertx.ext.couchdb;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;

public interface CouchDbDatabase {
  Future<JsonObject> status();

  CouchDbStream stream(JsonObject options);

  Future<JsonObject> createOrUpdateDesignDocument(String dbName, String designDocName, JsonObject designDocument);
}
