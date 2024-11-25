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
package io.vertx.ext.couchdb.database.impl;

import java.util.Objects;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.database.designdoc.DBDesignDoc;
import io.vertx.ext.couchdb.database.security.DBSecurity;
import io.vertx.ext.couchdb.parameters.BaseQueryParameters;
import io.vertx.ext.couchdb.parameters.DocumentGetParams;
import io.vertx.ext.couchdb.parameters.PathParameterTemplates;
import io.vertx.ext.couchdb.streams.CouchDbStream;
import io.vertx.uritemplate.UriTemplate;

public class CouchDbDatabaseImpl implements CouchDbDatabase {

  private final CouchdbClient client;

  private final String databaseName;

  /**
   * Create does create the JavaObject, not the couchDB
   *
   * @param client CouchDBClient
   * @param databaseName String
   * @return Future of CouchDbDatabase
   */
  public static Future<CouchDbDatabase> create(CouchdbClient client, String databaseName) {

    Promise<CouchDbDatabase> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.database(databaseName);

    client.doesExist(urlToCheck)
        .onSuccess(v -> promise.succeed(new CouchDbDatabaseImpl(client, databaseName)))
        .onFailure(promise::fail);

    return promise.future();

  }

  CouchDbDatabaseImpl(CouchdbClient client, String databaseName) {
    this.client = client;
    this.databaseName = databaseName;
  }

  @Override
  public String name() {
    return this.databaseName;
  }

  @Override
  public Future<JsonObject> createDocument(String docId, JsonObject document) {

    Objects.requireNonNull(docId);
    Objects.requireNonNull(document);

    Promise<JsonObject> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.databaseDocumentId(databaseName, docId);

    this.client.doesExist(urlToCheck)
        .onSuccess(v -> promise.fail("Document alreday exists"))
        .onFailure(
            err -> this.client.putJsonObject(urlToCheck, null, document)
                .onFailure(promise::fail)
                .onSuccess(promise::succeed));

    return promise.future();
  }

  @Override
  public Future<DBSecurity> getSecurity() {
    Promise<DBSecurity> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.databaseSecurity(databaseName);
    client.getJsonObject(urlToCheck, null)
        .onSuccess(json -> promise.complete(DBSecurity.fromJson(json)))
        .onFailure(promise::fail);
    return promise.future();
  }

  @Override
  public Future<JsonObject> setSecurity(DBSecurity dbSecurityObject) {

    Objects.requireNonNull(dbSecurityObject);
    JsonObject requestSecurityPayload = dbSecurityObject.toJson();
    Promise<JsonObject> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.databaseSecurity(databaseName);

    this.client.putJsonObject(urlToCheck, null, requestSecurityPayload)
        .onFailure(promise::fail)
        .onSuccess(a -> {
          JsonObject successResponse = new JsonObject().put("ok", true);
          promise.complete(successResponse);
        });

    return promise.future();
  }

  @Override
  public Future<JsonObject> updateDocument(String docId, String rev, JsonObject document) {

    Objects.requireNonNull(docId);
    Objects.requireNonNull(rev);
    Objects.requireNonNull(document);

    if (document.containsKey("_rev") && !rev.equals(document.getString("_rev"))) {
      return Future.failedFuture("Mismatch between ref parameter and _ref property");
    }
    BaseQueryParameters params = new BaseQueryParameters();
    params.addParameter("rev", rev, true);
    Promise<JsonObject> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.databaseDocumentId(databaseName, docId);

    this.client.getEtag(urlToCheck)
        .onSuccess(curRev -> {
          if (!curRev.equals(rev)) {
            promise.fail("Existing rev/ETag doesn't match rev param");
          } else {
            this.client.putJsonObject(urlToCheck, params, document)
                .onFailure(promise::fail)
                .onSuccess(promise::succeed);
          }
        })
        .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public Future<JsonObject> getDocument(String docId, DocumentGetParams options) {
    UriTemplate urlToCheck = PathParameterTemplates.databaseDocumentId(databaseName, docId);
    return client.getJsonObject(urlToCheck, options);
  }

  @Override
  public Future<JsonObject> status() {
    UriTemplate urlToCheck = PathParameterTemplates.database(databaseName);
    return this.client.getJsonObject(urlToCheck, null);
  }

  @Override
  public Future<Buffer> getDocumentAttachment(String docId, String attachementName,
      String rev) {

    Promise<Buffer> promise = Promise.promise();

    BaseQueryParameters params = new BaseQueryParameters();
    if (rev != null) {
      params.addParameter("rev", rev, true);
    }
    UriTemplate urlToCheck =
        PathParameterTemplates.attachment(this.databaseName, docId, attachementName);

    this.client.noBody(HttpMethod.GET, urlToCheck, params)
        .onSuccess(response -> promise.succeed(response.body()))
        .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteDocument(String docId, String rev, boolean force) {

    Promise<JsonObject> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.databaseDocumentId(databaseName, docId);
    this.client.getEtag(urlToCheck)
        .onFailure(promise::fail)
        .onSuccess(eTag -> {
          if (force || rev.equals(eTag)) {
            BaseQueryParameters params = new BaseQueryParameters();
            params.addParameter("rev", eTag);
            this.client.deleteJsonObject(urlToCheck, params)
                .onFailure(promise::fail)
                .onSuccess(promise::succeed);
          } else {
            promise.fail("rev / eTag mismatch");
          }

        });

    return promise.future();
  }

  @Override
  public CouchDbStream stream(JsonObject options) {
    throw new UnsupportedOperationException("Unimplemented method 'stream'");
  }

  @Override
  public Future<DBDesignDoc> getDesignDoc(String designDocName) {
    // TODO Auto-generated method stub
    Promise<DBDesignDoc> promise = Promise.promise();
    UriTemplate urlToCheck = PathParameterTemplates.databaseDesignDoc(databaseName, designDocName);
    client.getJsonObject(urlToCheck, null)
        .onSuccess(json -> promise.complete(DBDesignDoc.fromJson(json)))
        .onFailure(promise::fail);
    return promise.future();
  }

  @Override
  public Future<JsonObject> setDesignDoc(DBDesignDoc designDoc) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setDesignDoc'");
  }

  @Override
  public Future<JsonObject> deleteDesignDocument(String docId, String rev, boolean force) {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'setDesignDoc'");
  }
}
