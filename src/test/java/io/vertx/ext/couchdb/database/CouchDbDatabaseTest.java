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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import io.vertx.ext.couchdb.database.designdoc.DBDesignDoc;
import io.vertx.ext.couchdb.database.designdoc.DBDesignView;
import io.vertx.ext.couchdb.database.designdoc.ReduceOptions;
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
        .put(DBSecurity.ADMINS, new JsonObject()
            .put(DBSecurity.NAMES, new JsonArray().add("peter"))
            .put(DBSecurity.ROLES, new JsonArray().add("spiderMan")))
        .put(DBSecurity.MEMBERS, new JsonObject()
            .put(DBSecurity.NAMES, new JsonArray().add("richard"))
            .put(DBSecurity.ROLES, new JsonArray().add("tiger")));

    when(mockClient.getJsonObject(any(), any()))
        .thenReturn(Future.succeededFuture(res));


    database.getSecurity()
        .onSuccess(result -> testContext.verify(() -> {
          assertNotNull(result);
          assertTrue(result.getAdminNames().contains("peter"));
          assertTrue(result.getAdminRoles().contains("spiderMan"));
          assertTrue(result.getMemberNames().contains("richard"));
          assertTrue(result.getMemberRoles().contains("tiger"));

          assertFalse(result.getAdminNames().contains("spiderMan"));
          assertFalse(result.getAdminRoles().contains("peter"));
          assertFalse(result.getMemberNames().contains("tiger"));
          assertFalse(result.getMemberRoles().contains("richard"));

          assertFalse(result.getAdminNames().contains("richard"));
          assertFalse(result.getAdminRoles().contains("tiger"));
          assertFalse(result.getMemberNames().contains("peter"));
          assertFalse(result.getMemberRoles().contains("spiderMan"));

          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  /**
   * Tests successful security update operation for a database.
   *
   * @param testContext Vertx test context for asynchronous test execution
   * @throws InterruptedException if the test is interrupted while waiting for completion
   */
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

  /**
   * Tests successful retrieval of a design document from the database.
   *
   * @param testContext The Vert.x test context for asynchronous test execution
   * @throws InterruptedException if the test is interrupted while waiting for completion
   */
  @Test
  void testGetDesignDocumentSuccess(VertxTestContext testContext) throws InterruptedException {
    DBDesignDoc designDoc = new DBDesignDoc();
    DBDesignView view1 = new DBDesignView();
    view1.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view1.setReduce(ReduceOptions.COUNT);
    designDoc.setName("test-design-doc");
    designDoc.setLanguage("javascript");
    designDoc.getViews().put("test-view", view1);

    JsonObject res = designDoc.toJson();

    res.put("_id", "some_id");
    res.put("_rev", "some_rev");
    when(mockClient.getJsonObject(any(), any()))
        .thenReturn(Future.succeededFuture(res));


    database.getDesignDocument("test-design-doc")
        .onSuccess(result -> testContext.verify(() -> {
          assertNotNull(result);
          assertEquals(result.getViews().size(), 1);
          assertEquals(result.getViews().get("test-view").getMap(), "function (doc) {\\n" + //
              " emit(doc._id, 1);\\n" + //
              "}");
          assertEquals(result.getViews().get("test-view").getReduce().getValue(), "_count");
          assertEquals(result.getViews().get("test-view").getViewName(), "test-view");
          assertEquals(result.getLanguage(), "javascript");

          // to json test
          JsonObject resObj = result.toJson();
          JsonObject viewsObj = resObj.getJsonObject("views", new JsonObject());
          assertEquals(viewsObj.getJsonObject("test-view", new JsonObject()).getString("map"),
              "function (doc) {\\n" + //
                  " emit(doc._id, 1);\\n" + //
                  "}");
          assertEquals(viewsObj.getJsonObject("test-view", new JsonObject()).getString("reduce",
              ""),
              "_count");
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  /**
   * Tests the successful creation of a design document in the database.
   *
   * @param testContext The Vert.x test context for asynchronous test execution
   * @throws InterruptedException if the test is interrupted while waiting for completion
   */
  @Test
  void testCreateDesignDocumentSuccess(VertxTestContext testContext) throws InterruptedException {
    DBDesignDoc designDoc = new DBDesignDoc();
    DBDesignView view1 = new DBDesignView();
    view1.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view1.setReduce(ReduceOptions.STATS);
    DBDesignView view2 = new DBDesignView();
    view2.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view2.setReduce(ReduceOptions.SUM);
    designDoc.setName("test-design-doc");
    designDoc.setLanguage("javascript");
    designDoc.addView("test-view", view1);
    designDoc.addView("test-view2", view2);

    JsonObject res = new JsonObject()
        .put("ok", true)
        .put("id", "_design/test-design-doc")
        .put("rev", "somestring");

    when(mockClient.putJsonObject(any(), any(), any()))
        .thenReturn(Future.succeededFuture(res));


    database.createDesignDocument(designDoc)
        .onSuccess(result -> testContext.verify(() -> {
          assertNotNull(result);
          assertEquals(result.getBoolean("ok", false), true);
          assertEquals(result.getString("id", ""), "_design/test-design-doc");
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  /**
   * Tests the update functionality of a CouchDB design document.
   *
   * @param testContext The Vert.x test context for asynchronous test execution
   * @throws InterruptedException if the test execution is interrupted while waiting for completion
   *         This test verifies that:
   *         - Design document creation with multiple views
   *         - Proper setting of map/reduce functions
   *         - Successful update with correct ETag handling
   *         - Response validation including document ID and revision
   *         The test sets up a design document with two views using different reduce functions
   *         (STATS and SUM) and verifies the update operation through mock client responses.
   */
  @Test
  void testUpdateDesignDocument(VertxTestContext testContext) throws InterruptedException {
    DBDesignDoc designDoc = new DBDesignDoc();
    DBDesignView view1 = new DBDesignView();
    view1.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view1.setReduce(ReduceOptions.STATS);
    DBDesignView view2 = new DBDesignView();
    view2.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view2.setReduce(ReduceOptions.SUM);
    designDoc.setName("test-design-docId");
    designDoc.setLanguage("javascript");
    designDoc.addView("test-view", view1);
    designDoc.addView("test-view2", view2);

    JsonObject res = new JsonObject()
        .put("ok", true)
        .put("id", "_design/test-design-docId")
        .put("rev", "somestringinrevvalueAfterUpdate");

    when(mockClient.putJsonObject(any(), any(), any()))
        .thenReturn(Future.succeededFuture(res));
    when(mockClient.getEtag(any()))
        .thenReturn(Future.succeededFuture("somestringinrevvalueAfterUpdate"));


    database.updateDesignDocument(designDoc, "somestringinrevvalueAfterUpdate")
        .onSuccess(result -> testContext.verify(() -> {
          assertNotNull(result);
          assertEquals(result.getBoolean("ok", false), true);
          assertEquals(result.getString("id", ""), "_design/test-design-docId");
          assertEquals(result.getString("rev", ""), "somestringinrevvalueAfterUpdate");
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(5, TimeUnit.SECONDS));
  }

  /**
   * Tests the deletion of a design document from the database.
   *
   * @param testContext The Vert.x test context for asynchronous test execution
   * @throws InterruptedException if the test is interrupted while waiting for completion
   *         This test verifies that:
   *         - A design document with multiple views can be deleted successfully
   *         - The mock client returns the expected response
   *         - The response contains correct values for 'ok', 'id', and 'rev' fields
   *         - The deletion operation completes within the specified timeout
   */
  @Test
  void testDeleteDesignDocument(VertxTestContext testContext) throws InterruptedException {
    DBDesignDoc designDoc = new DBDesignDoc();
    DBDesignView view1 = new DBDesignView();
    view1.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view1.setReduce(ReduceOptions.STATS);
    DBDesignView view2 = new DBDesignView();
    view2.setMap("function (doc) {\\n" + //
        " emit(doc._id, 1);\\n" + //
        "}");
    view2.setReduce(ReduceOptions.SUM);
    designDoc.setName("test-design-docId");
    designDoc.setLanguage("javascript");
    designDoc.addView("test-view", view1);
    designDoc.addView("test-view2", view2);

    JsonObject res = new JsonObject()
        .put("ok", true)
        .put("id", "_design/test-design-docId")
        .put("rev", "somestringinrevvalue");

    when(mockClient.deleteJsonObject(any(), any()))
        .thenReturn(Future.succeededFuture(res));
    when(mockClient.getEtag(any())).thenReturn(Future.succeededFuture("somestringinrevvalue"));


    database.deleteDesignDocument(designDoc, "somestringinrevvalue")
        .onSuccess(result -> testContext.verify(() -> {
          assertNotNull(result);
          assertEquals(result.getBoolean("ok", false), true);
          assertEquals(result.getString("id", ""), "_design/test-design-docId");
          assertEquals(result.getString("rev", ""), "somestringinrevvalue");
          testContext.completeNow();
        }))
        .onFailure(err -> testContext.failNow(err));

    assertTrue(testContext.awaitCompletion(1, TimeUnit.SECONDS));
  }

}
