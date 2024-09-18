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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.parameters.DbCreateParams;
import io.vertx.ext.couchdb.testannotations.IntegrationTest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxTestContext;

@IntegrationTest
@Testcontainers
public class CouchdbClientIT {

  private static final int COUCHDB_PORT = 5984;

  @SuppressWarnings("resource")
  @Container
  private static final GenericContainer<?> couchdbContainer = new GenericContainer<>(
      DockerImageName.parse("couchdb:3.3.3"))
      .withExposedPorts(COUCHDB_PORT)
      .withEnv("COUCHDB_USER", "admin")
      .withEnv("COUCHDB_PASSWORD", "password")
      .waitingFor(Wait.forListeningPort());

  private static CouchdbClient client;

  private static CouchdbAdmin admin;

  @BeforeAll
  static void setup(Vertx vertx) {
    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
        .setDefaultHost(couchdbContainer.getHost())
        .setDefaultPort(couchdbContainer.getMappedPort(COUCHDB_PORT)));

    client = new CouchdbClientBuilder(vertx, webClient)
        .credentials(new UsernamePasswordCredentials("admin", "password"))
        .build();

    admin = CouchdbAdmin.get(client);

  }

  @AfterAll
  static void tearDown(Vertx vertx) {
    if (vertx != null) {
      vertx.close();
    }
    if (couchdbContainer != null) {
      couchdbContainer.stop();
    }
  }

  @Test
  void testCreateDbSuccess(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    Future<CouchDbDatabase> result = admin.createDb("new_db", new DbCreateParams());

    result
        .onFailure(testContext::failNow)
        .onSuccess(db -> testContext.verify(() -> {
          assertNotNull(db);
          assertEquals("new_db", db.name());
          testContext.completeNow();
        }));

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbAlreadyExists(Vertx vertx, VertxTestContext testContext)
      throws InterruptedException {
    admin.createDb("existing_db", new DbCreateParams())
        .onFailure(testContext::failNow)
        .onSuccess(db -> admin.createDb("existing_db", new DbCreateParams())
            // the second call must be a failure
            .onFailure(err -> {
              assertEquals("Bla", err.getMessage());
              testContext.completeNow();
            })
            .onSuccess(db2 -> testContext.failNow("double creation event")));

    testContext.awaitCompletion(10, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbInvalidName(Vertx vertx, VertxTestContext testContext)
      throws InterruptedException {
    admin.createDb("_invalid_db", null)
        .onFailure(err -> testContext.verify(() -> {
          assertEquals(
              "Not a valid database name",
              err.getMessage());
          testContext.completeNow();
        }))
        .onSuccess(
            db -> testContext.failNow("Expected failure for invalid database name but succeeded."));

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbUnauthorized(Vertx vertx, VertxTestContext testContext)
      throws InterruptedException {
    WebClient webClient = WebClient.create(vertx);

    CouchdbClient unauthorizedClient = new CouchdbClientBuilder(vertx, webClient)
        .credentials(new UsernamePasswordCredentials("invalid_user", "invalid_password"))
        .build();

    CouchdbAdmin badAdmin = CouchdbAdmin.get(unauthorizedClient);

    badAdmin.createDb("unauthorized_db", null).onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Error creating database: unauthorized - Name or password is incorrect.",
            ar.cause().getMessage());
        testContext.completeNow();
      } else {
        fail("Expected failure for unauthorized user but succeeded.");
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  protected static String getCouchdbHost() {
    return couchdbContainer.getHost();
  }

  protected static int getCouchdbPort() {
    return couchdbContainer.getMappedPort(COUCHDB_PORT);
  }
}
