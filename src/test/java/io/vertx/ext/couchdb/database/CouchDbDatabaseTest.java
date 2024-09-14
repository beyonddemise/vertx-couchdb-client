
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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import io.vertx.ext.couchdb.testannotations.UnitTest;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@UnitTest
class CouchDbDatabaseTest {

  @Mock
  private WebClient mockWebClient;

  @Mock
  private HttpRequest<Buffer> mockHttpRequest;

  @Mock
  private HttpResponse<Buffer> mockHttpResponse;

  private CouchdbClient client;

  private CouchDbDatabase database;

  @BeforeEach
  void setUp(Vertx vertx) {
    MockitoAnnotations.initMocks(this);
    client = CouchdbClient.create(vertx, mockWebClient,
        new UsernamePasswordCredentials("admin", "password"));
    database = CouchDbDatabase.create(client, "test_db");
  }

  @Test
  void testCreateOrUpdateDocumentSuccess(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.request(any(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.sendJson(any(JsonObject.class)))
        .thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(CREATED.code());
    when(mockHttpResponse.bodyAsJsonObject())
        .thenReturn(new JsonObject().put("ok", true).put("id", "recipe_123"));

    JsonObject document = new JsonObject()
        .put("name", "Spaghetti with meatballs")
        .put("ingredients", new JsonObject().put("pasta", "spaghetti"));

    database.createOrUpdateDocument("recipe_123", document).onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject result = ar.result();
        assertTrue(result.getBoolean("ok"));
        assertEquals("recipe_123", result.getString("id"));
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testCreateOrUpdateDocumentError(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.request(any(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.sendJson(any(JsonObject.class)))
        .thenReturn(Future.failedFuture(new Exception("Failed to create document")));

    JsonObject document = new JsonObject()
        .put("name", "Spaghetti with meatballs")
        .put("ingredients", new JsonObject().put("pasta", "spaghetti"));

    database.createOrUpdateDocument("recipe_123", document).onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Failed to create document", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testGetDocumentSuccess(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.request(any(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(OK.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject()
        .put("_id", "recipe_123")
        .put("name", "Spaghetti with meatballs")
        .put("ingredients", new JsonObject().put("pasta", "spaghetti")));

    JsonObject options = new JsonObject().put("attachments", false);

    database.getDocument("recipe_123", options).onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject result = ar.result();
        assertEquals("recipe_123", result.getString("_id"));
        assertEquals("Spaghetti with meatballs", result.getString("name"));
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testGetDocumentNotFound(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.request(any(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send())
        .thenReturn(Future.failedFuture(new Exception("Document not found")));

    JsonObject options = new JsonObject().put("attachments", false);

    database.getDocument("non_existing_doc", options).onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Document not found", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  @Test
  void testGetDocumentBadRequest(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.request(any(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.failedFuture(new Exception("Bad request")));

    JsonObject options = new JsonObject().put("attachments", false);

    database.getDocument("invalid_doc_id", options).onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Bad request", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }
}
