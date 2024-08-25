package io.vertx.ext.couchdb.exception;

import io.vertx.core.json.JsonObject;

public class CouchdbDatabaseCreationException extends Exception {
  private final String error;
  private final String reason;
  private final int statusCode;
  private final JsonObject responseBody;

  public CouchdbDatabaseCreationException(String error, String reason, int statusCode, JsonObject responseBody) {
    super("Error creating database: " + error + " - " + reason);
    this.error = error;
    this.reason = reason;
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  public String error() {
    return error;
  }

  public String reason() {
    return reason;
  }

  public int statusCode() {
    return statusCode;
  }

  public JsonObject responseBody() {
    return responseBody;
  }
}
