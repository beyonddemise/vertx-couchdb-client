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

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import io.vertx.core.Future;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.couchdb.admin.CouchdbAdmin;
import io.vertx.ext.couchdb.parameters.BaseQueryParameters;
import io.vertx.ext.couchdb.parameters.QueryParameters;
import io.vertx.ext.couchdb.testannotations.UnitTest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import io.vertx.uritemplate.UriTemplate;

@UnitTest
public class CouchdbClientTest {

  @Mock
  WebClient mockWebClient;

  @Mock
  HttpRequest<Buffer> mockHttpRequest;

  @Mock
  HttpResponse<Buffer> mockHttpResponse;

  CouchdbClient client;

  AutoCloseable mockCloseable;

  @BeforeEach
  void setUp(Vertx vertx, VertxTestContext testContext) {
    mockCloseable = MockitoAnnotations.openMocks(this);

    MultiMap headers = MultiMap.caseInsensitiveMultiMap();
    headers.add("ETag", "123456");
    headers.add("Content-Type", "application/json");

    client = new CouchdbClientBuilder(vertx, mockWebClient).build();
    lenient()
        .when(mockWebClient.request(any(HttpMethod.class), any(Integer.class), any(),
            any(UriTemplate.class)))
        .thenReturn(mockHttpRequest);

    lenient().when(mockHttpRequest.authentication(any(Credentials.class)))
        .thenReturn(mockHttpRequest);
    lenient().when(mockHttpRequest.ssl(any())).thenReturn(mockHttpRequest);
    lenient().when(mockHttpRequest.setTemplateParam(any(), anyMap())).thenReturn(mockHttpRequest);
    lenient().when(mockHttpRequest.sendJson(any()))
        .thenReturn(Future.succeededFuture(mockHttpResponse));
    lenient().when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));

    lenient().when(mockHttpResponse.statusCode()).thenReturn(200);
    lenient().when(mockHttpResponse.headers()).thenReturn(headers);

    testContext.completeNow();

  }

  @AfterEach
  void tearDown() throws Exception {
    mockCloseable.close();
    client = null;
  }

  @Test
  void testDeleteJsonObject(Vertx vertx, VertxTestContext testContext) {
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject());
    UriTemplate template = UriTemplate.of("/db/doc");
    QueryParameters params = new BaseQueryParameters();
    params.addParameter("rev", "1-xxx", true);
    client.deleteJsonObject(template, params)
        .onSuccess(json -> {
          testContext.verify(() -> {
            assertNotNull(json);
            verify(mockWebClient).request(eq(HttpMethod.DELETE), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testDeleteJsonObjectFail(Vertx vertx, VertxTestContext testContext) {
    when(mockHttpRequest.send()).thenReturn(Future.failedFuture(new RuntimeException("Failed to send request")));
    UriTemplate template = UriTemplate.of("/db/doc");
    QueryParameters params = new BaseQueryParameters();
    params.addParameter("rev", "1-xxx", true);
    client.deleteJsonObject(template, params)
        .onSuccess(json -> testContext.failNow("This call should have failed"))
        .onFailure(err -> testContext.verify(() -> {
          assertNotNull(err);
          assertEquals("java.lang.RuntimeException", err.getClass().getName());
          assertEquals("Failed to send request", err.getMessage());
          testContext.completeNow();
        }));

  }

  @Test
  void testDoesExist(Vertx vertx, VertxTestContext testContext) {
    UriTemplate template = UriTemplate.of("/db/doc");

    client.doesExist(template)
        .onSuccess(result -> {
          testContext.verify(() -> {
            verify(mockWebClient).request(eq(HttpMethod.HEAD), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testDoesExistFail(Vertx vertx, VertxTestContext testContext) {
    when(mockHttpResponse.statusCode()).thenReturn(404);
    UriTemplate template = UriTemplate.of("/db/doc");

    client.doesExist(template)
        .onSuccess(result -> testContext.failNow("This call should have failed"))
        .onFailure(err -> testContext.verify(() -> {
          assertNotNull(err);
          assertEquals("io.vertx.core.VertxException", err.getClass().getName());
          testContext.completeNow();
        }));
  }

  @Test
  void testGetAdmin(Vertx vertx, VertxTestContext testContext) {
    JsonObject session = new JsonObject()
        .put("ok", true)
        .put("userCtx", new JsonObject()
            .put("name", "admin")
            .put("roles", new JsonArray().add("_admin")));

    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(session);
    client.getAdmin()
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertTrue(result instanceof CouchdbAdmin);
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testGetDatabase(Vertx vertx, VertxTestContext testContext) {
    String dbName = "testdb";

    client.getDatabase(dbName)
        .onSuccess(database -> {
          testContext.verify(() -> {
            assertNotNull(database);
            assertEquals(dbName, database.name());
            verify(mockWebClient).request(eq(HttpMethod.HEAD), anyInt(), anyString(),
                any(UriTemplate.class));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testGetDatabaseFail(Vertx vertx, VertxTestContext testContext) {
    String dbName = "testdb";
    when(mockHttpResponse.statusCode()).thenReturn(404);
    client.getDatabase(dbName)
        .onSuccess(database -> testContext.failNow("This call should have failed"))
        .onFailure(err -> testContext.verify(() -> {
          assertNotNull(err);
          assertEquals("io.vertx.core.VertxException", err.getClass().getName());
          assertEquals("Response status code 404 is not [between 200 and 3]00", err.getMessage());
          testContext.completeNow();
        }));

  }

  @Test
  void testGetEtag(Vertx vertx, VertxTestContext testContext) {
    when(mockHttpResponse.getHeader("ETag")).thenReturn("123456");
    UriTemplate template = UriTemplate.of("/db/doc");
    client.getEtag(template)
        .onSuccess(etag -> {
          testContext.verify(() -> {
            assertNotNull(etag);
            verify(mockWebClient).request(eq(HttpMethod.HEAD), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);
  }

  @Test
  void testGetJsonArray(Vertx vertx, VertxTestContext testContext) {
    JsonArray expectedArray = new JsonArray().add("item1").add("item2");
    when(mockHttpResponse.bodyAsJsonArray()).thenReturn(expectedArray);
    UriTemplate template = UriTemplate.of("/test/array");

    client.getJsonArray(template, null)
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertEquals(expectedArray, result);
            verify(mockWebClient).request(eq(HttpMethod.GET), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testGetJsonObject(Vertx vertx, VertxTestContext testContext) {
    JsonObject expectedObject = new JsonObject().put("key", "value");
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(expectedObject);
    UriTemplate template = UriTemplate.of("/test/object");

    client.getJsonObject(template, null)
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertEquals(expectedObject, result);
            verify(mockWebClient).request(eq(HttpMethod.GET), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testNoBody(Vertx vertx, VertxTestContext testContext) {
    UriTemplate template = UriTemplate.of("/test/no-body");

    client.noBody(HttpMethod.GET, template, null)
        .onSuccess(response -> {
          testContext.verify(() -> {
            assertNotNull(response);
            assertEquals(200, response.statusCode());
            verify(mockWebClient).request(eq(HttpMethod.GET), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testPutJsonObject(Vertx vertx, VertxTestContext testContext) {
    JsonObject expectedObject = new JsonObject().put("key", "value");
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(expectedObject);
    UriTemplate template = UriTemplate.of("/test/put-object");

    client.putJsonObject(template, null)
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertEquals(expectedObject, result);
            verify(mockWebClient).request(eq(HttpMethod.PUT), anyInt(), anyString(), eq(template));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testPutJsonObjectFail(Vertx vertx, VertxTestContext testContext) {
    when(mockHttpResponse.statusCode()).thenReturn(404);
    UriTemplate template = UriTemplate.of("/test/put-object");

    client.putJsonObject(template, null)
        .onSuccess(result -> testContext.failNow("This call should have failed"))
        .onFailure(err -> testContext.verify(() -> {
          assertNotNull(err);
          assertEquals("io.vertx.core.VertxException", err.getClass().getName());
          testContext.completeNow();
        }));
  }

  @Test
  void testSession(Vertx vertx, VertxTestContext testContext) {
    JsonObject expectedObject = new JsonObject().put("key", "value");
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(expectedObject);

    client.session()
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertEquals(expectedObject, result);
            verify(mockWebClient).request(eq(HttpMethod.GET), anyInt(), anyString(),
                any(UriTemplate.class));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testStatus(Vertx vertx, VertxTestContext testContext) {
    JsonObject expectedObject = new JsonObject().put("key", "value");
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(expectedObject);

    client.status()
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertEquals(expectedObject, result);
            verify(mockWebClient).request(eq(HttpMethod.GET), anyInt(), anyString(),
                any(UriTemplate.class));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }

  @Test
  void testUuids(Vertx vertx, VertxTestContext testContext) {
    JsonObject expectedObject =
        new JsonObject().put("uuids", new JsonArray().add("item1").add("item2"));
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(expectedObject);

    client.uuids(3)
        .onSuccess(result -> {
          testContext.verify(() -> {
            assertNotNull(result);
            assertEquals(expectedObject, result);
            verify(mockWebClient).request(eq(HttpMethod.GET), anyInt(), anyString(),
                any(UriTemplate.class));
            verify(mockHttpRequest).send();
            testContext.completeNow();
          });
        })
        .onFailure(testContext::failNow);

  }
}
