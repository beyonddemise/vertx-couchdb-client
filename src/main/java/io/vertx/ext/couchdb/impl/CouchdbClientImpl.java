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
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.NullCredentials;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.exception.CouchdbException;
import io.vertx.ext.couchdb.parameters.BaseQueryParameters;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.WebClient;

public class CouchdbClientImpl implements CouchdbClient {

  private final VertxInternal vertx;
  private final WebClient client;
  private final Credentials credentials;

  CouchdbClientImpl(Vertx vertx, WebClient client, Credentials credentials) {
    Objects.requireNonNull(vertx);
    Objects.requireNonNull(client);
    Objects.requireNonNull(credentials);
    this.vertx = (VertxInternal) vertx;
    this.client = client;
    this.credentials = credentials;
  }

  public static CouchdbClient create(final Vertx vertx, final WebClient client) {
    return create(vertx, client, new NullCredentials());
  }

  public static CouchdbClient create(final Vertx vertx, final WebClient client,
      final Credentials credentials) {
    return new CouchdbClientImpl(vertx, client, credentials);
  }

  @Override
  public Future<JsonObject> status() {
    return this.getJsonObject(this.client, "/", null);
  }

  @Override
  public WebClient getWebClient() {
    return this.client;
  }

  @Override
  public Future<JsonObject> session() {
    String baseUrl = "/_session";
    return this.getJsonObject(this.client, baseUrl, null);
  }

  @Override
  public VertxInternal getVertx() {
    return this.vertx;
  }

  @Override
  public Credentials getCredentials() {
    return this.credentials;
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
    String baseUrl = "/_uuids";
    BaseQueryParameters param = new BaseQueryParameters();
    if (count > 0) {
      param.addParameter("count", count, true);
    }
    return this.getJsonObject(client, baseUrl, param);

  }

  @Override
  public Future<CouchDbDatabase> getDatabase(final String databaseName) {
    return CouchDbDatabase.create(this, databaseName);
  }


}
