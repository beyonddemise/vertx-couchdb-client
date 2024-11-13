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

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.exception.CouchdbException;
import io.vertx.ext.couchdb.parameters.QueryParameters;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.uritemplate.UriTemplate;

/**
 * A Vert.x service used to interact with CouchDB server instances.
 * <p>
 * Some of the operations might change <i>_id</i> field of passed
 * {@link JsonObject} document.
 *
 * @author <a href="https://wissel.net">Stephan Wissel</a>
 */
public interface CouchdbClient {

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
   * Retrieves a specified database.
   *
   * @param databaseName The name of the database, must follow specific naming
   *        rules.
   * @return Future with a CouchDbDatabase instance
   */
  Future<CouchDbDatabase> getDatabase(String databaseName);

  /**
   * @return CouchdbAdmin for administrative functions
   */
  Future<CouchdbAdmin> getAdmin();

  /**
   * Performs a GET request and returns the result as a JsonArray.
   *
   * @param baseUrl The base URL for the request.
   * @param params The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonArray result of the GET request.
   */
  Future<JsonArray> getJsonArray(UriTemplate baseUrl, QueryParameters params);

  /**
   * Performs a GET request and returns the result as a JsonObject.
   *
   * @param baseUrl The base URL for the request.
   * @param params The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonObject result of the GET request.
   */
  Future<JsonObject> getJsonObject(UriTemplate baseUrl, QueryParameters params);

  /**
   * Performs a PUT request without a body and returns the result as a JsonObject.
   *
   * @param baseUrl The base URL for the request.
   * @param params The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonObject result of the PUT request.
   */
  Future<JsonObject> putJsonObject(UriTemplate baseUrl, QueryParameters params);

  /**
   * Performs a PUT request with a body and returns the result as a JsonObject.
   *
   * @param baseUrl The base URL for the request.
   * @param params The query parameters to append to the URL, or null if none.
   * @param body The JSON body to send in the PUT request.
   * @return A Future with the JsonObject result of the PUT request.
   */
  Future<JsonObject> putJsonObject(UriTemplate baseUrl, QueryParameters params, JsonObject body);

  /**
   * Performs a DELETE request and returns the result as a JsonObject.
   *
   * @param baseUrl The base URL for the request.
   * @param params The query parameters to append to the URL, or null if none.
   * @return A Future with the JsonObject result of the PUT request.
   */
  Future<JsonObject> deleteJsonObject(UriTemplate baseUrl, QueryParameters params);

  /**
   * Checks if a resource exists by performing a HEAD request.
   *
   * @param urlToCheck UriTemplate The URL to check for existence.
   * @return A Future that completes when the request is complete.
   */
  Future<Void> doesExist(UriTemplate urlToCheck);

  /**
   * Retrieves the ETag header from a HEAD request.
   *
   * @param urlToCheck The URL to retrieve the ETag from.
   * @return A Future with the ETag header value.
   */
  Future<String> getEtag(UriTemplate urlToCheck);

  /**
   * Performs a HttpRequest request using the provided UriTemplate and
   * QueryParameters.
   *
   * @param method The HttpMethod to be used for the request.
   * @param baseUrl The UriTemplate representing the base URL for the request.
   * @param params The QueryParameters to be applied to the request.
   * @return A Future containing the HttpResponse with a Buffer body.
   */
  Future<HttpResponse<Buffer>> noBody(HttpMethod method, UriTemplate baseUrl,
      QueryParameters params);

  /**
   * Closes the client and releases all associated resources.
   */
  void close();
}
