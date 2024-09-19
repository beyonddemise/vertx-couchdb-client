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

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.concurrent.TimeUnit;

import com.github.dockerjava.api.command.InspectContainerResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.parameters.BaseQueryParameters;
import io.vertx.ext.couchdb.parameters.DbCreateParams;
import io.vertx.ext.couchdb.testannotations.IntegrationTest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import io.vertx.uritemplate.UriTemplate;

@IntegrationTest
@Testcontainers
@TestMethodOrder(OrderAnnotation.class)
public class CouchdbClientIT {

  static final int COUCHDB_PORT = 5984;
  static final String COUCHDB_USER = "admin";
  static final String COUCHDB_PASSWORD = "password";
  static final String COUCHDB_IMAGE = "apache/couchdb:latest";
  static final Credentials COUCHDB_CREDENTIALS = new UsernamePasswordCredentials(COUCHDB_USER,
      COUCHDB_PASSWORD);
  static final String DB_NAME = "testdb";

  @SuppressWarnings("resource")
  @Container
  static GenericContainer<?> couchdbContainer = new GenericContainer<>(
      DockerImageName.parse(COUCHDB_IMAGE))
      .withExposedPorts(COUCHDB_PORT)
      .withEnv("COUCHDB_USER", COUCHDB_USER)
      .withEnv("COUCHDB_PASSWORD", COUCHDB_PASSWORD)
      .waitingFor(Wait.forListeningPort());

  CouchdbClient client;

  @BeforeAll
  static void setup(Vertx vertx, VertxTestContext testContext) throws InterruptedException {

    InspectContainerResponse containerInfo = couchdbContainer.getContainerInfo();
    System.out.println(containerInfo.getState());

    WebClient webClient = WebClient.create(vertx);

    CouchdbClient client = new CouchdbClientBuilder(vertx, webClient)
        .credentials(COUCHDB_CREDENTIALS)
        .host(couchdbContainer.getHost())
        .port(couchdbContainer.getMappedPort(COUCHDB_PORT))
        .build();

    client.getAdmin()
        .onSuccess(admin -> {
          admin.checkOrCreateSystemDatabases()
              .onFailure(testContext::failNow)
              .onSuccess(v -> testContext.completeNow());
        })
        .onFailure(testContext::failNow);

  }

  @AfterAll
  static void tearDown(Vertx vertx) {
    if (vertx != null) {
      vertx.close();
    }
    if (couchdbContainer != null) {
      couchdbContainer.stop();
    }
    vertx = null;
    couchdbContainer = null;
  }

  @BeforeEach
  void beforeEach(Vertx vertx) {
    client = new CouchdbClientBuilder(vertx, WebClient.create(vertx))
        .credentials(COUCHDB_CREDENTIALS)
        .host(couchdbContainer.getHost())
        .port(couchdbContainer.getMappedPort(COUCHDB_PORT))
        .build();
  }

  @AfterEach
  void afterEach() {
    client.close();
  }

  @Test
  @Order(1)
  void testStatus(Vertx vertx, VertxTestContext testContext) throws InterruptedException {

    client.status()
        .onFailure(testContext::failNow)
        .onSuccess(status -> {
          testContext.verify(() -> {
            assertNotNull(status);
            testContext.completeNow();
          });
        });
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  @Order(2)
  void testSession(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    client.session()
        .onFailure(testContext::failNow)
        .onSuccess(session -> {
          testContext.verify(() -> {
            assertNotNull(session);
            assertTrue(session.containsKey("ok"));
            assertTrue(session.getBoolean("ok"));
            assertTrue(session.containsKey("userCtx"));
            JsonObject userCtx = session.getJsonObject("userCtx");
            assertNotNull(userCtx);
            assertEquals(COUCHDB_USER, userCtx.getString("name"));
            testContext.completeNow();
          });
        });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);

  }

  @Test
  @Order(3)
  void testUuids(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    client.uuids(5)
        .onFailure(testContext::failNow)
        .onSuccess(uuidsObj -> {
          testContext.verify(() -> {
            assertNotNull(uuidsObj);
            assertTrue(uuidsObj.containsKey("uuids"));
            JsonArray uuids = uuidsObj.getJsonArray("uuids");
            assertEquals(5, uuids.size());
            uuids.forEach(uuid -> {
              assertNotNull(uuid);
              assertTrue(uuid instanceof String);
              assertTrue(((String) uuid).matches("[a-f0-9]{32}"));
            });
            testContext.completeNow();
          });
        });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);

  }

  @Test
  @Order(4)
  void testCreateDb(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    client.getAdmin()
        .onSuccess(admin -> {
          admin.createDb(DB_NAME, new DbCreateParams())
              .onFailure(testContext::failNow)
              .onSuccess(v -> testContext.completeNow());
        })
        .onFailure(testContext::failNow);
  }

  @Test
  @Order(5)
  void testCreateDbFail(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    client.getAdmin()
        .onSuccess(admin -> {
          admin.createDb(DB_NAME, new DbCreateParams())
              .onFailure(err -> testContext.verify(() -> {
                assertNotNull(err);
                testContext.completeNow();
              }))
              .onSuccess(v -> testContext.failNow("db exists, should not be created"));
        })
        .onFailure(testContext::failNow);
  }

  @Test
  @Order(6)
  void testGetDatabase(Vertx vertx, VertxTestContext testContext) throws InterruptedException {

    client.getDatabase(DB_NAME)
        .onFailure(testContext::failNow)
        .onSuccess(database -> {
          testContext.verify(() -> {
            assertNotNull(database);
            assertEquals(DB_NAME, database.name());
            testContext.completeNow();
          });
        });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);

  }

  @Test
  @Order(7)
  void testDoesExist(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/_users");
    client.doesExist(urlToCheck)
        .onFailure(testContext::failNow)
        .onSuccess(exists -> testContext.completeNow());
  }

  @Test
  @Order(8)
  void testDoesNotExist(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/blabla");
    client.doesExist(urlToCheck)
        .onFailure(err -> testContext.verify(() -> {
          testContext.completeNow();
        }))
        .onSuccess(exists -> testContext.failNow("blabla should not exist"));
  }

  @Test
  @Order(9)
  void testPutJsonObject(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/anotherdb");
    client.putJsonObject(urlToCheck, new BaseQueryParameters())
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertEquals("io.vertx.core.json.JsonObject", response.getClass().getName());
            assertTrue(response.getBoolean("ok"));
            testContext.completeNow();
          });
        });
  }

  @Test
  @Order(10)
  void testPutJsonObjectWithBody(Vertx vertx, VertxTestContext testContext) {
    JsonObject body = new JsonObject().put("name", "John").put("age", 30);
    UriTemplate urlToCheck = UriTemplate.of("/" + DB_NAME + "/doc");
    client.putJsonObject(urlToCheck, new BaseQueryParameters(), body)
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertEquals("io.vertx.core.json.JsonObject", response.getClass().getName());
            assertTrue(response.getBoolean("ok"));
            assertNotNull(response.getString("rev"));
            assertEquals("doc", response.getString("id"));
            testContext.completeNow();
          });
        });
  }

  @Test
  @Order(11)
  void testDeleteJsonObject(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/anotherdb");
    client.deleteJsonObject(urlToCheck, new BaseQueryParameters())
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertEquals("io.vertx.core.json.JsonObject", response.getClass().getName());
            assertTrue(response.getBoolean("ok"));
            testContext.completeNow();
          });
        });
  }

  @Test
  @Order(12)
  void testGetEtag(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/" + DB_NAME + "/doc");
    client.getEtag(urlToCheck)
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertNotNull(response);
            testContext.completeNow();
          });
        });
  }

  @Test
  @Order(13)
  void testGetEtagFail(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of(DB_NAME + "/nonexistentdoc");
    client.getEtag(urlToCheck)
        .onFailure(v -> testContext.completeNow())
        .onSuccess(response -> testContext.failNow("nonexistent doc should not return etag"));
  }

  @Test
  @Order(14)
  void testGetJsonArray(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/_all_dbs");
    client.getJsonArray(urlToCheck, new BaseQueryParameters())
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertNotNull(response);
            assertEquals("io.vertx.core.json.JsonArray", response.getClass().getName());
            assertEquals(5, response.size());
            testContext.completeNow();
          });
        });
  }

  @Test
  @Order(15)
  void testGetJsonObject(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/" + DB_NAME + "/doc");
    client.getJsonObject(urlToCheck, new BaseQueryParameters())
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertNotNull(response);
            assertEquals("io.vertx.core.json.JsonObject", response.getClass().getName());
            assertEquals("John", response.getString("name"));
            assertEquals(30, response.getInteger("age"));
            testContext.completeNow();
          });
        });
  }

  @Test
  @Order(16)
  void testNoBody(Vertx vertx, VertxTestContext testContext) {
    UriTemplate urlToCheck = UriTemplate.of("/anotherdb");
    client.noBody(HttpMethod.PUT, urlToCheck, new BaseQueryParameters())
        .onFailure(testContext::failNow)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertNotNull(response);
            assertEquals(201, response.statusCode());
            JsonObject jsonResponse = response.bodyAsJsonObject();
            assertEquals("io.vertx.core.json.JsonObject", jsonResponse.getClass().getName());
            assertTrue(jsonResponse.getBoolean("ok"));
            testContext.completeNow();
          });
        });
  }

}
