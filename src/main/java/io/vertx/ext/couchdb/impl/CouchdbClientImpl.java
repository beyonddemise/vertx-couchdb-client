package io.vertx.ext.couchdb.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.exception.CouchdbDatabaseCreationException;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

import java.util.Objects;

public class CouchdbClientImpl implements CouchdbClient {

  private final VertxInternal vertx;
  private final WebClient client;
  private final Credentials credentials;

  CouchdbClientImpl(Vertx vertx, WebClient client, Credentials credentials) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(client);
    this.vertx = (VertxInternal) vertx;
    this.client = client;
    this.credentials = credentials;
  }

  public static CouchdbClient create(final Vertx vertx, final WebClient client) {
    return create(vertx, client, null);
  }

  public static CouchdbClient create(final Vertx vertx, final WebClient client, final Credentials credentials) {
    return new CouchdbClientImpl(vertx, client, credentials);
  }

  @Override
  public Future<JsonObject> status() {
    Promise<JsonObject> promise = vertx.promise();
    HttpRequest<Buffer> request = client.get("/");

    if (credentials != null) {
      request.authentication(credentials);
    }
    request.send()
      .onFailure(promise::fail)
      .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();
  }

  @Override
  public Future<JsonArray> activeTasks() {
    throw new UnsupportedOperationException("Unimplemented method 'activeTasks'");
  }

  @Override
  public Future<JsonArray> allDbs() {
    throw new UnsupportedOperationException("Unimplemented method 'allDbs'");
  }

  @Override
  public Future<JsonArray> allDbs(JsonObject options) {
    throw new UnsupportedOperationException("Unimplemented method 'allDbs'");
  }

  @Override
  public Future<JsonArray> dbsInfo(JsonObject options) {
    throw new UnsupportedOperationException("Unimplemented method 'dbsInfo'");
  }

  @Override
  public Future<Buffer> rawCall(JsonObject params) {
    Promise<Buffer> promise = vertx.promise();

    String methodString = params.getString("method", "GET");
    String path = params.getString("path", "/");
    HttpMethod method = HttpMethod.valueOf(methodString.toUpperCase());
    HttpRequest<Buffer> request = client.request(method, path);

    if (credentials != null) {
      request.authentication(credentials);
    }

    if (params.containsKey("body")) {
      JsonObject body = params.getJsonObject("body");
      request.sendJson(body)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.body()));
    } else {
      request.send()
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.body()));
    }

    return promise.future();
  }
  @Override
  public Future<JsonObject> createDb(String databaseName) {
    return createDb(databaseName, new JsonObject());
  }

  @Override
  public Future<JsonObject> createDb(String databaseName, JsonObject options) {
    options.put("db_name", databaseName);
    return doCreateDb(options);
  }

  @Override
  public Future<CouchDbDatabase> getDatabase(final String databaseName) {
    return Future.succeededFuture(CouchDbDatabase.create(this, databaseName));
  }

  private Future<JsonObject> doCreateDb(JsonObject options) {
    Promise<JsonObject> promise = vertx.promise();
    var databaseName = options.getString("db_name");
    var shards = options.getInteger("q", 8);
    var replicas = options.getInteger("n", 3);
    var partitioned = options.getBoolean("partitioned", false);

    HttpRequest<Buffer> request = client.put("/" + databaseName)
      .addQueryParam("q", shards.toString())
      .addQueryParam("n", replicas.toString())
      .addQueryParam("partitioned", partitioned.toString());

    if (credentials != null) {
      request.authentication(credentials);
    }

    request.send()
      .onSuccess(response -> {
        int statusCode = response.statusCode();
        JsonObject result = response.bodyAsJsonObject();
        if (statusCode == 201 || statusCode == 202) {
          promise.complete(result);
        } else {
          String error = result.getString("error");
          String reason = result.getString("reason");
          promise.fail(new CouchdbDatabaseCreationException(error, reason, statusCode, result));
        }
      })
      .onFailure(promise::fail);

    return promise.future();
  }
}
