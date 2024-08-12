package io.vertx.ext.couchdb.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.CouchdbClientOptions;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

public class CouchdbClientImpl implements CouchdbClient {

  public static CouchdbClient create(Vertx vertx, CouchdbClientOptions clientOptions) {
    return new CouchdbClientImpl(vertx, clientOptions);
  }

  private final VertxInternal vertx;
  private final CouchdbClientOptions clientOptions;
  private WebClient client = null;

  CouchdbClientImpl(Vertx vertx, CouchdbClientOptions clientOptions) {
    this.vertx = (VertxInternal) vertx;
    this.clientOptions = clientOptions;
  }

  @Override
  public Future<JsonObject> status() {
    Promise<JsonObject> promise = Promise.promise();
    initClient();
    HttpRequest<?> request = client.get("/");

    if (clientOptions.getCredentials() != null) {
      request.authentication(clientOptions.getCredentials().toAuthCredentials());
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
    Promise<Buffer> promise = Promise.promise();

    String methodString = params.getString("method", "GET");
    String path = params.getString("path", "/");
    HttpMethod method = HttpMethod.valueOf(methodString.toUpperCase());
    initClient();
    HttpRequest<Buffer> request = client.request(method, path);

    if (clientOptions.getCredentials() != null) {
      request.authentication(clientOptions.getCredentials().toAuthCredentials());
    }
    request.send()
      .onFailure(promise::fail)
      .onSuccess(response -> promise.complete(response.body()));

    return promise.future();
  }

  private void initClient() {
    if (client == null) {
      client = WebClient.create(vertx, clientOptions.getWebClientOptions());
    }
  }

  @Override
  public Future<JsonObject> createDb(JsonObject options) {
    Promise<JsonObject> promise = Promise.promise();
    var databaseName = options.getString("db_name");
    var shards = options.getInteger("q", 8);
    var replicas = options.getInteger("n", 3);
    var partitioned = options.getBoolean("partitioned", false);

    initClient();
    HttpRequest<Buffer> request = client.put("/" + databaseName)
      .addQueryParam("q", shards.toString())
      .addQueryParam("n", replicas.toString())
      .addQueryParam("partitioned", partitioned.toString());

    if (clientOptions.getCredentials() != null) {
      request.authentication(clientOptions.getCredentials().toAuthCredentials());
    }

    request.send()
      .onSuccess(response -> {
        int statusCode = response.statusCode();
        JsonObject result = response.bodyAsJsonObject();
        if (statusCode == 201 || statusCode == 202) {
          promise.complete(result);
        } else {
          promise.fail(new RuntimeException(
            "Error creating database: " + result.getString("error") + " - " + result.getString("reason")));
        }
      })
      .onFailure(promise::fail);

    return promise.future();
  }




}
