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
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.exception.CouchdbException;
import io.vertx.ext.couchdb.impl.CouchdbClientImpl;
import io.vertx.ext.couchdb.parameters.CouchdbQueryParams;
import io.vertx.ext.web.client.WebClient;

/**
 * A Vert.x service used to interact with CouchDB server instances.
 * <p>
 * Some of the operations might change <i>_id</i> field of passed {@link JsonObject} document.
 *
 * @author <a href="https://wissel.net">Stephan Wissel</a>
 */
@VertxGen
public interface CouchdbClient {

  /**
   * Create a CouchDB client with a WebClient instance.
   *
   * @param vertx the Vert.x instance
   * @param client the WebClient instance
   * @return the client
   */
  static CouchdbClient create(Vertx vertx, WebClient client) {
    return CouchdbClientImpl.create(vertx, client);
  }

  /**
   * Create a CouchDB client with a WebClient instance and credentials.
   *
   * @param vertx the Vert.x instance
   * @param client the WebClient instance
   * @param credentials the credentials for authentication
   * @return the client
   */
  static CouchdbClient create(Vertx vertx, WebClient client, Credentials credentials) {
    return CouchdbClientImpl.create(vertx, client, credentials);
  }

  /**
   * CouchDB instance status as JSON object.
   *
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#/
   * @return Future with the status of the CouchDB server, fails with
   *         {@link CouchdbException} if the operation fails.
   */
  Future<JsonObject> status();

  /**
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#active-tasks
   * @return Future with task informaton
   */
  Future<JsonArray> activeTasks(CouchdbQueryParams options);

  /**
   * List all databases.
   *
   * @See https://docs.couchdb.org/en/stable/api/server/common.html#all-dbs
   * @return Future with the list of databases, fails with {@link CouchdbException} if the operation
   *         fails.
   */
  Future<JsonArray> allDbs();

  /**
   * List all databases, limited by the query parameters.
   *
   * @param options JsonObject with the query parameters.
   * @return Future with the list of databases, fails with {@link CouchdbException} if the operation
   *         fails.
   */
  Future<JsonArray> allDbs(CouchdbQueryParams options);

  /**
   * List all database Info, limited by the query parameters.
   *
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#dbs-info
   * @param options JsonObject with the query parameters.
   * @return Future with the list of databases, fails with {@link CouchdbException} if the operation
   *         fails.
   */
  Future<JsonArray> dbsInfo(CouchdbQueryParams options);

  /**
   * Makes a call to the CouchDB server with the given parameters.
   *
   * @param params JsonObject with the headers, path, method, parameters, and payload (if any).
   * @return Future with the result of the call, fails with {@link CouchdbException} if the
   *         operation fails.
   */
  Future<Buffer> rawCall(JsonObject params);

  /**
   * Makes a call to the CouchDB server with the given parameters.
   * Expects a Json Object back
   *
   * @param params JsonObject with the headers, path, method, parameters, and payload (if any).
   * @return Future with the result of the call, fails with {@link CouchdbException} if the
   *         operation fails.
   */
  Future<Buffer> jsonObjectCall(JsonObject params);


  /**
   * Makes a call to the CouchDB server with the given parameters.
   * expects a JsonArray back
   *
   * @param params JsonObject with the headers, path, method, parameters, and payload (if any).
   * @return Future with the result of the call, fails with {@link CouchdbException} if the
   *         operation fails.
   */
  Future<Buffer> jsonArrayCall(JsonObject params);


  /**
   * Creates a new database in CouchDB with the specified name and options.
   *
   * @param databaseName The name of the database, must follow specific naming rules.
   * @param options JsonObject containing optional parameters for creating the database.
   * @return Future with the result of the create operation, containing the response from CouchDB.
   */
  Future<CouchDbDatabase> createDb(String databaseName, JsonObject options);

  /**
   * Retrieves a specified database.
   *
   * @param databaseName The name of the database, must follow specific naming rules.
   * @return Future with a CouchDbDatabase instance
   */
  Future<CouchDbDatabase> getDatabase(String databaseName);
}
