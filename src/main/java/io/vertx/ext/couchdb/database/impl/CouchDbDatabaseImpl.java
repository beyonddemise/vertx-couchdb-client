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
import io.vertx.core.http.HttpResponseExpectation;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchDbStream;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.parameters.BaseQueryParameters;
import io.vertx.ext.couchdb.parameters.DocumentGetParams;
import io.vertx.uritemplate.UriTemplate;
import io.vertx.uritemplate.Variables;

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

    client.doesExist(client.getWebClient(), "/" + databaseName)
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
    String urlToCheck = String.format("/%s/%s", this.databaseName, docId);

    this.client.doesExist(this.client.getWebClient(), urlToCheck)
        .onSuccess(v -> promise.fail("Document alreday exists"))
        .onFailure(
            err -> this.client.putJsonObject(this.client.getWebClient(), urlToCheck, null, document)
                .onFailure(promise::fail)
                .onSuccess(promise::succeed));

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
    String urlToCheck = String.format("/%s/%s", this.databaseName, docId);

    this.client.getEtag(this.client.getWebClient(), urlToCheck)
        .onSuccess(curRev -> {
          if (!curRev.equals(rev)) {
            promise.fail("Existing rev/ETag doesn't match rev param");
          } else {
            this.client.putJsonObject(this.client.getWebClient(), urlToCheck, params, document)
                .onFailure(promise::fail)
                .onSuccess(promise::succeed);
          }
        })
        .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public Future<JsonObject> getDocument(String docId, DocumentGetParams options) {
    String urlToCheck = String.format("/%s/%s", this.databaseName, docId);
    return client.getJsonObject(this.client.getWebClient(), urlToCheck, options);
  }



  @Override
  public Future<JsonObject> status() {
    String urlToCheck = String.format("/%s/", databaseName);
    return this.client.getJsonObject(this.client.getWebClient(), urlToCheck, null);
  }

  @Override
  public Future<Buffer> getDocumentAttachment(String docId, String attachementName,
      String rev) {

    Promise<Buffer> promise = Promise.promise();

    BaseQueryParameters params = new BaseQueryParameters();
    if (rev != null) {
      params.addParameter("rev", rev, true);
    }
    UriTemplate template = UriTemplate.of("/{db}/{doc}/{attname}");
    Variables variables = Variables.variables()
        .set("db", this.databaseName)
        .set("doc", docId)
        .set("attname", attachementName);

    String urlToCheck = params.appendParamsToUrl(template.expandToString(variables));

    this.client.getWebClient().get(urlToCheck)
        .authentication(this.client.getCredentials())
        .send()
        .expecting(HttpResponseExpectation.SC_OK)
        .onSuccess(response -> promise.succeed(response.body()))
        .onFailure(promise::fail);

    return promise.future();
  }

  @Override
  public Future<JsonObject> deleteDocument(String docId, String rev, boolean force) {

    Promise<JsonObject> promise = Promise.promise();

    String urlToCheck = String.format("/%s/%s", this.databaseName, docId);
    this.client.getEtag(this.client.getWebClient(), urlToCheck)
        .onFailure(promise::fail)
        .onSuccess(eTag -> {
          if (force || rev.equals(eTag)) {

            this.client.getWebClient().delete(urlToCheck)
                .addQueryParam("rev", eTag)
                .send()
                .expecting(HttpResponseExpectation.SC_OK)
                .expecting(HttpResponseExpectation.JSON)
                .onFailure(promise::fail)
                .onSuccess(response -> promise.succeed(response.bodyAsJsonObject()));
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
}
