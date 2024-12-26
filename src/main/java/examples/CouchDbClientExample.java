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
package examples;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.CouchdbClientBuilder;
import io.vertx.ext.web.client.WebClient;

public class CouchDbClientExample {

  /**
   * Creates and initializes a CouchDB client using the provided Vertx instance.
   *
   * @param vertx the Vertx instance used to create the WebClient and CouchDB client
   * @throws NullPointerException if vertx parameter is null
   * @see io.vertx.ext.web.client.WebClient
   * @see CouchdbClient
   */
  public void create(Vertx vertx) {
    WebClient webClient = WebClient.create(vertx);
    CouchdbClientBuilder builder = new CouchdbClientBuilder(vertx, webClient);
    CouchdbClient couchDbClient = builder.build();
  }

  public void createAuthenticated(Vertx vertx, Credentials credentials) {
    WebClient webClient = WebClient.create(vertx);
    CouchdbClientBuilder builder = new CouchdbClientBuilder(vertx, webClient);
    builder.credentials(credentials);
    CouchdbClient couchDbClient = builder.build();
  }

  public void createFull(Vertx vertx, Credentials credentials, int port, String host,
      boolean isTls) {
    WebClient webClient = WebClient.create(vertx);
    CouchdbClient couchDbClient = new CouchdbClientBuilder(vertx, webClient)
        .credentials(credentials)
        .host(host)
        .port(port)
        .https(isTls)
        .build();
  }


  // TODO: implement more
}
