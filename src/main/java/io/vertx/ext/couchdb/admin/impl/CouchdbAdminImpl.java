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
import io.vertx.ext.couchdb.parameters.PathParameterTemplates;
import io.vertx.uritemplate.UriTemplate;

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
    UriTemplate baseUrl = UriTemplate.of("/_active_tasks");
    return this.client.getJsonArray(baseUrl, null);
  }

  @Override
  public Future<JsonArray> allDbs() {
    return allDbs(new DbQueryParams());
  }

  @Override
  public Future<JsonArray> allDbs(DbQueryParams options) {
    UriTemplate baseUrl = UriTemplate.of("/_all_dbs" + PathParameterTemplates.QUERY);
    return this.client.getJsonArray(baseUrl, options);
  }

  @Override
  public Future<JsonArray> dbsInfo(DbQueryParams options) {
    UriTemplate baseUrl = UriTemplate.of("/_dbs_info" + PathParameterTemplates.QUERY);
    ;
    return this.client.getJsonArray(baseUrl, options);
  }

  @Override
  public Future<CouchDbDatabase> createDb(String databaseName, DbCreateParams options) {

    if (!isValidDbName(databaseName)) {
      return Future.failedFuture("Not a valid database name");
    }

    Promise<CouchDbDatabase> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.database(databaseName);

    // This call success means db exists and thos promise fails
    this.client.doesExist(urlToCheck)
        .onFailure(err ->
        // DB Doesn't exist, so we can create it
        this.client.putJsonObject(urlToCheck, options)
            .onFailure(promise::fail) /* TODO: capture error/reason */
            .onSuccess(v -> CouchDbDatabase.create(this.client, databaseName)))
        .onSuccess(v -> promise.fail("database does exist"));
    return promise.future();
  }

  public static boolean isValidDbName(String databaseName) {
    return Pattern.matches("^[a-z][a-z0-9_$()+/-]*$", databaseName);
  }

  // TODO: add admin actions
}
