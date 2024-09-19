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

import static org.junit.Assert.assertNotNull;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.testannotations.UnitTest;
import io.vertx.ext.web.client.WebClient;

@UnitTest
public class CouchdbClientBuilderTest {

  WebClient webClient;
  CouchdbClientBuilder builder;

  @BeforeEach
  void setUp(Vertx vertx) {
    webClient = WebClient.create(vertx);
    builder = new CouchdbClientBuilder(vertx, webClient);
  }

  @AfterEach
  void tearDown() {
    webClient.close();
    webClient = null;
    builder = null;
  }

  @Test
  void testBuild(Vertx vertx) {
    CouchdbClient couchDbClient = builder.build();
    assertNotNull(couchDbClient);
  }

  @Test
  void testBuild2(Vertx vertx) {
    CouchdbClient couchDbClient = builder
        .host("tango.com")
        .port(5984)
        .https(true)
        .credentials(new UsernamePasswordCredentials("admin", "password"))
        .build();
    assertNotNull(couchDbClient);
  }

}
