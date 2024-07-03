package io.vertx.ext.couchdb.impl;

import io.vertx.core.Vertx;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.CouchdbClientOptions;

public class CouchdbClientImpl implements CouchdbClient {

  public static CouchdbClient create(Vertx vertx, CouchdbClientOptions options) {
    return new CouchdbClientImpl(vertx, options);
  }

  private final Vertx vertx;
  private final CouchdbClientOptions options;

  CouchdbClientImpl(Vertx vertx, CouchdbClientOptions options) {
    this.vertx = vertx;
    this.options = options;
  }


}
