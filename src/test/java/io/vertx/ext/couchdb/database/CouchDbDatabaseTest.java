
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
package io.vertx.ext.couchdb.database;

import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.security.DBSecurity;
import io.vertx.ext.couchdb.parameters.DocumentGetParams;
import io.vertx.ext.couchdb.testannotations.UnitTest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;

@UnitTest
class CouchDbDatabaseTest {

  @Mock
  private WebClient mockWebClient;

  @Mock
  private CouchdbClient mockClient;
  @Mock
  private HttpRequest<Buffer> mockHttpRequest;

  @Mock
  private HttpResponse<Buffer> mockHttpResponse;

  @Mock
  private DBSecurity mockDbSecurity;

  private CouchDbDatabase database;

  AutoCloseable mockCloseable;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    mockCloseable = MockitoAnnotations.openMocks(this);

    when(mockClient.doesExist(any())).thenReturn(Future.succeededFuture());

    CouchDbDatabase.create(mockClient, "test_db")
        .onFailure(testContext::failNow)
        .onSuccess(db -> {
          database = db;
          testContext.completeNow();
        });

  }

  @AfterEach
  void tearDown() {
    try {
      mockCloseable.close();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  @Test
  void testCreateDocumentSuccess(VertxTestContext testContext) throws InterruptedException {

    JsonObject document = new JsonObject();
    JsonObject resultDoc = new JsonObject().put("ok", true).put("id", "recipe_123");

    when(mockClient.doesExist(any())).thenReturn(Future.failedFuture("Document already exists"));
    when(mockClient.putJsonObject(any(), any(), any()))
        .thenReturn(Future.succeededFuture(resultDoc));

    database.createDocument("recipe_123", document)
        .onFailure(testContext::failNow)
        .onSuccess(result -> testContext.verify(() -> {
          verify(mockClient, times(2)).doesExist(any());
          assertTrue(result.getBoolean("ok"));
          testContext.completeNow();
        }));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testUpdateDocumentSuccess(VertxTestContext testContext) throws InterruptedException {

    JsonObject document = new JsonObject();
    JsonObject resultDoc = new JsonObject().put("ok", true).put("id", "recipe_123");

    when(mockClient.getEtag(any())).thenReturn(Future.succeededFuture("1-23456"));
    when(mockClient.putJsonObject(any(), any(), any()))
        .thenReturn(Future.succeededFuture(resultDoc));

    database.updateDocument("recipe_123", "1-23456", document)
        .onFailure(testContext::failNow)
        .onSuccess(result -> testContext.verify(() -> {
          verify(mockClient).doesExist(any());
          assertTrue(result.getBoolean("ok"));
          testContext.completeNow();
        }));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testCreateDocumentFailure(VertxTestContext testContext) throws InterruptedException {

    JsonObject document = new JsonObject();

    when(mockClient.doesExist(any())).thenReturn(Future.succeededFuture());

    database.createDocument("recipe_123", document)
        .onFailure(err -> testContext.verify(() -> {
          assertNotNull(err);
          verify(mockClient, times(2)).doesExist(any());
          testContext.completeNow();
        }))
        .onSuccess(result -> testContext.failNow("That Test should not pass"));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testUpdateDocumentfailure(VertxTestContext testContext) throws InterruptedException {

    JsonObject document = new JsonObject();

    when(mockClient.getEtag(any())).thenReturn(Future.succeededFuture("999996"));

    database.updateDocument("recipe_123", "1-23456", document)
        .onFailure(err -> testContext.verify(() -> {
          assertNotNull(err);
          testContext.completeNow();
        }))
        .onSuccess(result -> testContext.failNow("This call should fail"));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testGetDocumentSuccess(VertxTestContext testContext) throws InterruptedException {

    when(mockClient.getJsonObject(any(), any())).thenReturn(Future.succeededFuture(new JsonObject()
        .put("_id", "recipe_123")
        .put("name", "Spaghetti with meatballs")
        .put("ingredients", new JsonObject().put("pasta", "spaghetti"))));

    DocumentGetParams options = new DocumentGetParams().conflicts(false);

    database.getDocument("recipe_123", options)
        .onSuccess(result -> testContext.verify(() -> {
          assertEquals("recipe_123", result.getString("_id"));
          assertEquals("Spaghetti with meatballs", result.getString("name"));
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testGetDocumentNotFound(VertxTestContext testContext) throws InterruptedException {
    when(mockClient.getJsonObject(any(), any()))
        .thenReturn(Future.failedFuture(new Exception("Document not found")));

    DocumentGetParams options = new DocumentGetParams().conflicts(false);

    database.getDocument("non_existing_doc", options)
        .onFailure(err -> testContext.verify(() -> {
          assertEquals("Document not found", err.getMessage());
          testContext.completeNow();
        }))
        .onSuccess(result -> testContext.failNow("Expected to fail, but succeeded"));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testGetSecuritySuccess(VertxTestContext testContext) throws InterruptedException {
    JsonObject res = new JsonObject()
        .put("admins",
            new JsonObject().put("names", new JsonArray().add("peter"))
                .put("roles", new JsonArray().add("spiderMan")))
        .put("members",
            new JsonObject().put("names", new JsonArray().add("richard")).put("roles",
                new JsonArray().add("tiger")));
    when(mockClient.getJsonObject(any(), any())).thenReturn(Future.succeededFuture(res));


    database.getSecurity()
        .onSuccess(result -> testContext.verify(() -> {
          assertEquals("peter", result.getJsonObject("admins").getJsonArray("names").getString(0));
          assertEquals("richard",
              result.getJsonObject("members").getJsonArray("names").getString(0));
          assertEquals("spiderMan",
              result.getJsonObject("admins").getJsonArray("roles").getString(0));
          assertEquals("tiger", result.getJsonObject("members").getJsonArray("roles").getString(0));
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testUpdateSecuritySuccess(VertxTestContext testContext) throws InterruptedException {
    JsonObject payload = new JsonObject()
        .put("admins",
            new JsonObject().put("names", new JsonArray().add("updatedPeter")).put("roles",
                "updatedSpiderMan"))
        .put("members",
            new JsonObject().put("names", new JsonArray().add("updatedRichard")).put("roles",
                "updatedTiger"));
    when(mockClient.putJsonObject(any(), any(),
        any())).thenReturn(Future.succeededFuture(payload));


    database.setSecurity(mockDbSecurity)
        .onSuccess(result -> testContext.verify(() -> {
          assertEquals("true", result.getString("ok"));
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

}
