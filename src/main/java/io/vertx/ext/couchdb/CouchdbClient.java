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

package io.vertx.ext.couchdb;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.exception.CouchdbException;
import io.vertx.ext.couchdb.impl.CouchdbClientImpl;
import io.vertx.ext.couchdb.parameters.QueryParameters;
import io.vertx.ext.web.client.WebClient;

/**
 * A Vert.x service used to interact with CouchDB server instances.
 * <p>
 * Some of the operations might change <i>_id</i> field of passed
 * {@link JsonObject} document.
 *
 * @author <a href="https://wissel.net">Stephan Wissel</a>
 */
@VertxGen
public interface CouchdbClient {

  /**
   * Create a CouchDB client with a WebClient instance.
   *
   * @param vertx  the Vert.x instance
   * @param client the WebClient instance
   * @return the client
   */
  static CouchdbClient create(Vertx vertx, WebClient client) {
    return CouchdbClientImpl.create(vertx, client);
  }

  /**
   * Create a CouchDB client with a WebClient instance and credentials.
   *
   * @param vertx       the Vert.x instance
   * @param client      the WebClient instance
   * @param credentials the credentials for authentication
   * @return the client
   */
  static CouchdbClient create(Vertx vertx, WebClient client, Credentials credentials) {
    return CouchdbClientImpl.create(vertx, client, credentials);
  }

  /**
   * CouchDB server instance status as JSON object.
   *
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#/
   * @return Future with the status of the CouchDB server, fails with
   *         {@link CouchdbException} if the operation fails.
   */
  Future<JsonObject> status();

  /**
   * CouchDB session status as JSON object.
   *
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#/
   * @return Future with the status of the CouchDB server, fails with
   *         {@link CouchdbException} if the operation fails.
   */
  Future<JsonObject> session();

  /**
   * request one or more uuids
   *
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#_uuids
   * @return Future with the status of the CouchDB server, fails with
   *         {@link CouchdbException} if the operation fails.
   */
  Future<JsonObject> uuids(int count);

  /**
   * Makes a call to the CouchDB server with the given parameters.
   *
   * @param params JsonObject with the headers, path, method, parameters, and
   *               payload (if any).
   * @return Future with the result of the call, fails with
   *         {@link CouchdbException} if the
   *         operation fails.
   */
  Future<Buffer> rawCall(JsonObject params);

  /**
   * Retrieves a specified database.
   *
   * @param databaseName The name of the database, must follow specific naming
   *                     rules.
   * @return Future with a CouchDbDatabase instance
   */
  Future<CouchDbDatabase> getDatabase(String databaseName);

  /**
   * @return the vertx instance
   */
  VertxInternal getVertx();

  /**
   * @return Credentials used
   */
  Credentials getCredentials();

  /**
   * @return Client in use
   */
  WebClient getWebClient();

  /**
   * @return CouchdbAdmin for administrative functions
   */
  Future<CouchdbAdmin> getAdmin();

  /**
   * Performs a GET request and returns the result as a JsonArray.
   *
   * @param client  The WebClient to use for the request.
   * @param baseUrl The base URL for the request.
   * @param params  The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonArray result of the GET request.
   */
  default Future<JsonArray> getJsonArray(WebClient client, String baseUrl, QueryParameters params) {

    String finalUrl = params == null ? baseUrl : params.appendParamsToUrl(baseUrl);

    Promise<JsonArray> promise = getVertx().promise();
    client.get(finalUrl)
        .authentication(getCredentials())
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonArray()));

    return promise.future();

  }

  /**
   * Performs a GET request and returns the result as a JsonObject.
   *
   * @param client  The WebClient to use for the request.
   * @param baseUrl The base URL for the request.
   * @param params  The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonObject result of the GET request.
   */
  default Future<JsonObject> getJsonObject(WebClient client, String baseUrl,
      QueryParameters params) {

    String finalUrl = params == null ? baseUrl : params.appendParamsToUrl(baseUrl);

    Promise<JsonObject> promise = getVertx().promise();
    client.get(finalUrl)
        .authentication(getCredentials())
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();

  }

  /**
   * Performs a PUT request and returns the result as a JsonObject.
   *
   * @param client  The WebClient to use for the request.
   * @param baseUrl The base URL for the request.
   * @param params  The query parameters to append to the URL, or null if none.
   * @param body    The JSON body to send in the PUT request.
   * @return A Future with the JsonObject result of the PUT request.
   */
  default Future<JsonObject> putJsonObject(WebClient client, String baseUrl,
      QueryParameters params, JsonObject body) {

    String finalUrl = params == null ? baseUrl : params.appendParamsToUrl(baseUrl);

    Promise<JsonObject> promise = getVertx().promise();
    client.put(finalUrl)
        .authentication(getCredentials())
        .sendJson(body)
        .expecting(HttpResponseExpectation.SC_OK)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();

  }

  /**
   * Performs a PUT request and returns the result as a JsonObject.
   *
   * @param client  The WebClient to use for the request.
   * @param baseUrl The base URL for the request.
   * @param params  The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonObject result of the PUT request.
   */
  default Future<JsonObject> putJsonObject(WebClient client, String baseUrl,
      QueryParameters params) {

    String finalUrl = params == null ? baseUrl : params.appendParamsToUrl(baseUrl);

    Promise<JsonObject> promise = getVertx().promise();
    client.put(finalUrl)
        .authentication(getCredentials())
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .expecting(HttpResponseExpectation.JSON)
        .onFailure(promise::fail)
        .onSuccess(response -> promise.complete(response.bodyAsJsonObject()));

    return promise.future();

  }

  /**
   * Checks if a resource exists by performing a HEAD request.
   *
   * @param client     The WebClient to use for the request.
   * @param urlToCheck The URL to check for existence.
   * @return A Future that completes when the request is complete.
   */
  default Future<Void> doesExist(WebClient client, String urlToCheck) {

    Promise<Void> promise = Promise.promise();

    client.head(urlToCheck)
        .authentication(getCredentials())
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .onSuccess(v -> promise.succeed())
        .onFailure(promise::fail);

    return promise.future();
  }

  /**
   * Retrieves the ETag header from a HEAD request.
   *
   * @param client     The WebClient to use for the request.
   * @param urlToCheck The URL to retrieve the ETag from.
   * @return A Future with the ETag header value.
   */
  default Future<String> getEtag(WebClient client, String urlToCheck) {

    Promise<String> promise = Promise.promise();

    client.head(urlToCheck)
        .authentication(getCredentials())
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .onSuccess(response -> promise.succeed(response.getHeader("ETag")))
        .onFailure(promise::fail);

    return promise.future();
  }
}
