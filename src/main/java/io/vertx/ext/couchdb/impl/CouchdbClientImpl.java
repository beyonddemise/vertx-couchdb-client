package io.vertx.ext.couchdb.impl;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;

public class CouchdbClientImpl implements CouchdbClient {

  public static CouchdbClient create(Vertx vertx, WebClientOptions options) {
    return new CouchdbClientImpl(vertx, options);
  }

  private final Vertx vertx;
  private final WebClientOptions options;
  private WebClient client = null;
  private Credentials credentials;

  CouchdbClientImpl(Vertx vertx, WebClientOptions options) {
    this.vertx = vertx;
    this.options = options;
  }

  @Override
  public Future<JsonObject> status() {

    Promise<JsonObject> promise = Promise.promise();
    initClient();
    HttpRequest<?> request = client.get("/");

    if (this.credentials != null) {
      request.authentication(this.credentials);
    }
    request.send()
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();
  }

  @Override
  public Future<JsonArray> activeTasks() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'activeTasks'");
  }

  @Override
  public Future<JsonArray> allDbs() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'allDbs'");
  }

  @Override
  public Future<JsonArray> allDbs(JsonObject options) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'allDbs'");
  }

  @Override
  public Future<JsonArray> dbsInfo(JsonObject options) {
    // TODO Auto-generated method stub
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

    if (this.credentials != null) {
      request.authentication(this.credentials);
    }
    request.send()
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.body()));

    return promise.future();
  }

  private void initClient() {
    if (client == null) {
      client = WebClient.create(vertx, options);
    }
  }

  @Override
  public CouchdbClient authetication(Credentials credentials) {
    this.credentials = credentials;
    return this;
  }

}
