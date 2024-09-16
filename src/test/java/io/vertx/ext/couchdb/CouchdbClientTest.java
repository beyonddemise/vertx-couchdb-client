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

import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
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
    lenient().when(mockWebClient.request(any(HttpMethod.class), any(Integer.class), any(), any(UriTemplate.class)))
        .thenReturn(mockHttpRequest);

    lenient().when(mockHttpRequest.authentication(any(Credentials.class))).thenReturn(mockHttpRequest);
    lenient().when(mockHttpRequest.ssl(any())).thenReturn(mockHttpRequest);
    lenient().when(mockHttpRequest.setTemplateParam(any(), anyMap())).thenReturn(mockHttpRequest);
    lenient().when(mockHttpRequest.sendJson(any())).thenReturn(Future.succeededFuture(mockHttpResponse));
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
  void testGetAdmin(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testGetDatabase(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testGetEtag(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testGetJsonArray(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testGetJsonObject(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testNoBody(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testPutJsonObject(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testPutJsonObject2(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testSession(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testStatus(Vertx vertx, VertxTestContext testContext) {

  }

  @Test
  void testUuids(Vertx vertx, VertxTestContext testContext) {

  }
}
