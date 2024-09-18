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
package io.vertx.ext.couchdb.impl;

import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.exception.CouchdbException;
import io.vertx.ext.couchdb.parameters.BaseQueryParameters;
import io.vertx.ext.couchdb.parameters.PathParameterTemplates;
import io.vertx.ext.couchdb.parameters.QueryParameters;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.uritemplate.UriTemplate;

public class CouchdbClientImpl implements CouchdbClient {

  private final VertxInternal vertx;
  private final WebClient client;
  private final Credentials credentials;
  private final String host;
  private final int port;
  private final boolean https;

  public CouchdbClientImpl(final Vertx vertx, final WebClient client,
      final String host, final int port, final boolean https, final Credentials credentials) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(client);
    Objects.requireNonNull(host);
    Objects.requireNonNull(port);
    Objects.requireNonNull(https);
    Objects.requireNonNull(credentials);

    this.vertx = (VertxInternal) vertx;
    this.client = client;
    this.credentials = credentials;
    this.host = host;
    this.port = port;
    this.https = https;
  }

  @Override
  public Future<JsonObject> status() {
    return this.getJsonObject(UriTemplate.of("/"), null);
  }

  @Override
  public Future<JsonObject> session() {
    UriTemplate baseUrl = UriTemplate.of("/_session");
    return this.getJsonObject(baseUrl, null);
  }

  @Override
  public Future<CouchdbAdmin> getAdmin() {
    Promise<CouchdbAdmin> promise = vertx.promise();
    this.status()
        .onFailure(promise::fail)
        .onSuccess(json -> {
          JsonArray roles = json.getJsonObject("userCtx", new JsonObject())
              .getJsonArray("roles", new JsonArray());
          if (roles.contains("_admin")) {
            promise.complete(null);
          } else {
            promise.fail(new CouchdbException("You are not Admin"));
          }
        });

    return promise.future();
  }

  @Override
  @Deprecated
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
  public Future<JsonObject> uuids(int count) {
    UriTemplate baseUrl = UriTemplate.of("/_uuids" + PathParameterTemplates.QUERY);
    BaseQueryParameters param = new BaseQueryParameters();
    if (count > 0) {
      param.addParameter("count", count, true);
    }
    return this.getJsonObject(baseUrl, param);

  }

  @Override
  public Future<CouchDbDatabase> getDatabase(final String databaseName) {
    return CouchDbDatabase.create(this, databaseName);
  }

  @Override
  public Future<JsonArray> getJsonArray(UriTemplate baseUrl, QueryParameters params) {

    Promise<JsonArray> promise = this.vertx.promise();
    this.noBody(HttpMethod.GET, baseUrl, params)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonArray()));

    return promise.future();

  }

  @Override
  public Future<JsonObject> getJsonObject(UriTemplate baseUrl, QueryParameters params) {

    Promise<JsonObject> promise = this.vertx.promise();

    this.noBody(HttpMethod.GET, baseUrl, params)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();

  }

  @Override
  public Future<JsonObject> putJsonObject(UriTemplate baseUrl, QueryParameters params) {

    Promise<JsonObject> promise = this.vertx.promise();
    this.noBody(HttpMethod.PUT, baseUrl, params)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));
    return promise.future();

  }

  @Override
  public Future<JsonObject> putJsonObject(UriTemplate baseUrl, QueryParameters params, JsonObject body) {

    return this.jsonBody(HttpMethod.PUT, baseUrl, params, body);
  }

  @Override
  public Future<Void> doesExist(UriTemplate urlToCheck) {

    Promise<Void> promise = Promise.promise();
    this.noBody(HttpMethod.HEAD, urlToCheck, null)
        .onSuccess(v -> promise.succeed())
        .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public Future<String> getEtag(UriTemplate urlToCheck) {

    Promise<String> promise = Promise.promise();

    noBody(HttpMethod.HEAD, urlToCheck, null)
        .onSuccess(response -> {
          String etag = response.getHeader("ETag");
          if (etag != null) {
            promise.succeed(etag);
          } else {
            promise.fail(new CouchdbException("ETag header not found in response"));
          }
        })
        .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public Future<HttpResponse<Buffer>> noBody(HttpMethod method, UriTemplate baseUrl, QueryParameters params) {

    QueryParameters actualParams = params == null ? new BaseQueryParameters() : params;

    return client.request(method, this.port, this.host, baseUrl)
        .setTemplateParam("query", actualParams.forTemplate())
        .authentication(this.credentials)
        .ssl(this.https)
        .send()
        .expecting(HttpResponseExpectation.SC_OK);
  }

  /**
   * Performs a HttpRequest request using the provided UriTemplate and
   * QueryParameters.
   *
   * @param method  The HttpMethod to be used for the request.
   * @param baseUrl The UriTemplate representing the base URL for the request.
   * @param params  The QueryParameters to be applied to the request.
   * @param body    The JsonObject body to be sent in the request.
   * @return A Future containing the HttpResponse with a Buffer body.
   */
  Future<JsonObject> jsonBody(HttpMethod method, UriTemplate baseUrl,
      QueryParameters params, JsonObject body) {

    Promise<JsonObject> promise = this.vertx.promise();
    QueryParameters actualParams = params == null ? new BaseQueryParameters() : params;

    client.request(method, this.port, this.host, baseUrl)
        .setTemplateParam("query", actualParams.forTemplate())
        .authentication(this.credentials)
        .ssl(this.https)
        .sendJson(body)
        .expecting(HttpResponseExpectation.SC_OK)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteJsonObject(UriTemplate baseUrl, QueryParameters params) {
    Promise<JsonObject> promise = this.vertx.promise();
    this.noBody(HttpMethod.DELETE, baseUrl, params)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();
  }

}
