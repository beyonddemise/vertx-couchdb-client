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

import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.database.CouchDbDatabase;
import io.vertx.ext.couchdb.parameters.DbCreateParams;
import io.vertx.ext.couchdb.testannotations.UnitTest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

@UnitTest
class CouchdbClientTest {

  @Mock
  private WebClient mockWebClient;

  @Mock
  private HttpRequest<Buffer> mockHttpRequest;

  @Mock
  private HttpResponse<Buffer> mockHttpResponse;

  private CouchdbClient client;

  private CouchdbAdmin admin;

  @BeforeEach
  void setUp(Vertx vertx) {
    lenient().when(mockWebClient.head(anyString())).thenReturn(mockHttpRequest);
    client = CouchdbClient.create(vertx, mockWebClient,
        new UsernamePasswordCredentials("admin", "password"));
    admin = CouchdbAdmin.get(client);
  }

  @Test
  void testCreate() {
    assertNotNull(client);
  }

  @Test
  void testCreateAdmin() {
    assertNotNull(admin);
  }

  @Test
  @Disabled("To be refactored")
  void testStatus(VertxTestContext testContext) throws InterruptedException {
    Future<JsonObject> future = client.status();
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject status = ar.result();
        assertNotNull(status);
        assertEquals("Welcome", status.getString("couchdb"));
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbSuccess(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockWebClient.head(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(CREATED.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("ok", true));

    admin.createDb("new_db", new DbCreateParams())
        .onFailure(testContext::failNow)
        .onSuccess(db -> testContext.verify(() -> {
          assertNotNull(db);
          assertEquals("new_db", db.name());
          testContext.completeNow();
        }));

  }

  @Test
  void testCreateDbDatabaseAlreadyExists(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockWebClient.head(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(OK.code());
    when(mockHttpResponse.bodyAsJsonObject())
        .thenReturn(new JsonObject().put("error", "file_exists").put("reason",
            "The database could not be created, the file already exists."));

    Future<CouchDbDatabase> result = admin.createDb("existing_db", new DbCreateParams());

    result.onComplete(ar -> {
      if (ar.failed()) {
        assertEquals(
            "database does exist",
            ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbInvalidDatabaseName(VertxTestContext testContext) throws InterruptedException {

    Future<CouchDbDatabase> result = admin.createDb("_invalid_db", new DbCreateParams());

    result.onComplete(ar -> {
      if (ar.failed()) {
        assertEquals(
            "Not a valid database name",
            ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbUnauthorized(Vertx vertx, VertxTestContext testContext)
      throws InterruptedException {
    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockWebClient.head(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.failedFuture(
        new Exception("Error creating database: unauthorized - Name or password is incorrect.")));

    CouchdbClient unauthorizedClient = CouchdbClient.create(vertx, mockWebClient,
        new UsernamePasswordCredentials("invalid_user", "invalid_password"));
    CouchdbAdmin badadmin = CouchdbAdmin.get(unauthorizedClient);

    badadmin.createDb("unauthorized_db", new DbCreateParams()).onComplete(ar -> {
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

}
