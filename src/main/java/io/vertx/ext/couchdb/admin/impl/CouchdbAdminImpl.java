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
package io.vertx.ext.couchdb.admin.impl;

import java.util.regex.Pattern;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.parameters.DbCreateParams;
import io.vertx.ext.couchdb.parameters.DbQueryParams;

public class CouchdbAdminImpl implements CouchdbAdmin {

  final CouchdbClient client;

  public CouchdbAdminImpl(CouchdbClient client) {
    this.client = client;
  }

  @Override
  public CouchdbClient getClient() {
    return this.client;
  }

  @Override
  public Future<JsonArray> activeTasks() {
    String baseUrl = "/_active_tasks";
    return this.client.getJsonArray(this.client.getWebClient(), baseUrl, null);
  }

  @Override
  public Future<JsonArray> allDbs() {
    return allDbs(new DbQueryParams());
  }

  @Override
  public Future<JsonArray> allDbs(DbQueryParams options) {
    String baseUrl = "/_all_dbs";
    return this.client.getJsonArray(this.client.getWebClient(), baseUrl, options);
  }

  @Override
  public Future<JsonArray> dbsInfo(DbQueryParams options) {
    String baseUrl = "/_dbs_info";
    return this.client.getJsonArray(this.client.getWebClient(), baseUrl, options);
  }


  @Override
  public Future<CouchDbDatabase> createDb(String databaseName, DbCreateParams options) {

    if (!isValidDbName(databaseName)) {
      return Future.failedFuture("Not a valid database name");
    }

    Promise<CouchDbDatabase> promise = Promise.promise();
    String urlToCheck = "/" + databaseName;

    // This call success means db exists and thos promise fails
    this.client.doesExist(this.client.getWebClient(), urlToCheck)
        .onFailure(err ->
        // DB Doesn't exist, so we can create it
        this.client.putJsonObject(this.client.getWebClient(), urlToCheck, options)
            .onFailure(promise::fail) /* TODO: capture error/reason */
            .onSuccess(v -> CouchDbDatabase.create(this.client, databaseName)))
        .onSuccess(v -> promise.fail("database does exist"));
    return promise.future();
  }


  public static boolean isValidDbName(String databaseName) {
    return Pattern.matches("^[a-z][a-z0-9_$()+/-]*$", databaseName);
  }

}
