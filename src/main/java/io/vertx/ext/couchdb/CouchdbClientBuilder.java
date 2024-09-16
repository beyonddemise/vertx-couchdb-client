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

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.impl.CouchdbClientImpl;
import io.vertx.ext.web.client.WebClient;

/**
 * Builder for creating a CouchdbClient instance.
 * <p>
 * This class allows for the configuration of various parameters needed to create a CouchdbClient,
 * including the host, port, HTTPS usage, and authentication credentials.
 * <p>
 * Usage example:
 * <pre>
 * {@code
 * CouchdbClient client = new CouchdbClientBuilder(vertx, webClient)
 *     .host("couchdb.example.com")
 *     .port(5984)
 *     .setHttps(true)
 *     .credentials(myCredentials)
 *     .build();
 * }
 * </pre>
 */

/**
 * Create a CouchdbClient using the Builder pattern
 * set host, port, https, credentials
 */
public class CouchdbClientBuilder {

  final Vertx vertx;
  final WebClient client;

  boolean https = false;
  Credentials credentials = new NullCredentials();
  int port = 5984;
  String host = "localhost";

  /**
   * Constructor for CouchdbClientBuilder.
   *
   * @param vertx  The Vertx instance to use for the client.
   * @param client The WebClient instance to use for the client.
   */
  public CouchdbClientBuilder(Vertx vertx, WebClient client) {
    this.vertx = vertx;
    this.client = client;
  }

  /**
   * Set the credentials for the client.
   *
   * @param credentials The credentials to use for the client.
   * @return The CouchdbClientBuilder instance.
   */
  public CouchdbClientBuilder credentials(Credentials credentials) {
    this.credentials = credentials;
    return this;
  }

  /**
   * Set the port for the client.
   *
   * @param port The port to use for the client.
   * @return The CouchdbClientBuilder instance.
   */
  public CouchdbClientBuilder port(int port) {
    this.port = port;
    return this;
  }

  /**
   * Set the host for the client.
   *
   * @param host The host to use for the client.
   * @return The CouchdbClientBuilder instance.
   */
  public CouchdbClientBuilder host(String host) {
    this.host = host;
    return this;
  }

  /**
   * Set the HTTPS usage for the client.
   *
   * @param https The boolean value to use for the client.
   * @return The CouchdbClientBuilder instance.
   */
  public CouchdbClientBuilder setHttps(boolean https) {
    this.https = https;
    return this;
  }

  /**
   * Build the CouchdbClient instance.
   *
   * @return The CouchdbClient instance.
   */
  public CouchdbClient build() {
    return new CouchdbClientImpl(vertx, client, host, port, https, credentials);
  }

}
