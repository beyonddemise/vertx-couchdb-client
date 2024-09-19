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
package io.vertx.ext.couchdb.admin;

import io.vertx.core.Future;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.admin.impl.CouchdbAdminImpl;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.exception.CouchdbException;
import io.vertx.ext.couchdb.parameters.DbCreateParams;
import io.vertx.ext.couchdb.parameters.DbQueryParams;

/**
 * Collection of methods that require admin priviledges
 */
public interface CouchdbAdmin {

  static CouchdbAdmin create(CouchdbClient client) {
    return new CouchdbAdminImpl(client);
  }

  /**
   * @return the client used to retrieve the admin
   */
  CouchdbClient getClient();

  /**
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#active-tasks
   * @return Future with task informaton
   */
  Future<JsonArray> activeTasks();

  /**
   * List all databases.
   *
   * @See https://docs.couchdb.org/en/stable/api/server/common.html#all-dbs
   * @return Future with the list of databases, fails with
   *         {@link CouchdbException} if the operation
   *         fails.
   */
  Future<JsonArray> allDbs();

  /**
   * List all databases, limited by the query parameters.
   *
   * @param options JsonObject with the query parameters.
   * @return Future with the list of databases, fails with
   *         {@link CouchdbException} if the operation
   *         fails.
   */
  Future<JsonArray> allDbs(DbQueryParams options);

  /**
   * List all database Info, limited by the query parameters.
   *
   * @see https://docs.couchdb.org/en/stable/api/server/common.html#dbs-info
   * @param options JsonObject with the query parameters.
   * @return Future with the list of databases, fails with
   *         {@link CouchdbException} if the operation
   *         fails.
   */
  Future<JsonArray> dbsInfo(DbQueryParams options);

  /**
   * Creates a new database in CouchDB with the specified name and options.
   *
   * @param databaseName The name of the database, must follow specific naming
   *        rules.
   * @param options JsonObject containing optional parameters for creating
   *        the database.
   * @return Future with the result of the create operation, containing the
   *         response from CouchDB.
   */
  Future<CouchDbDatabase> createDb(String databaseName, DbCreateParams options);

  /**
   * Checks if the system databases exist, creates them if needed
   *
   * @return Future with the result of the check operation, containing the
   *         response from CouchDB.
   */
  Future<JsonObject> checkOrCreateSystemDatabases();

  // TODO: implement more

}
