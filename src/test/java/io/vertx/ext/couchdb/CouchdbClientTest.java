package io.vertx.ext.couchdb;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.testannotations.UnitTest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.PRECONDITION_FAILED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@UnitTest
class CouchdbClientTest {

  @Mock
  private WebClient mockWebClient;

  @Mock
  private HttpRequest<Buffer> mockHttpRequest;

  @Mock
  private HttpResponse<Buffer> mockHttpResponse;

  private CouchdbClient client;

  @BeforeEach
  void setUp(Vertx vertx) {
    client = CouchdbClient.create(vertx, mockWebClient, new UsernamePasswordCredentials("admin", "password"));
  }

  @Test
  void testCreate() {
    assertNotNull(client);
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
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(CREATED.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("ok", true));

    Future<JsonObject> result = client.createDb("new_db");

    result.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject dbResult = ar.result();
        assertNotNull(dbResult);
        assertTrue(dbResult.getBoolean("ok"));
        testContext.completeNow();
      } else {
        testContext.failNow(ar.cause());
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbDatabaseAlreadyExists(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(PRECONDITION_FAILED.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("error", "file_exists").put("reason", "The database could not be created, the file already exists."));

    Future<JsonObject> result = client.createDb("existing_db");

    result.onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Error creating database: file_exists - The database could not be created, the file already exists.", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbInvalidDatabaseName(VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(BAD_REQUEST.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("error", "illegal_database_name").put("reason", "Name: '_invalid_db'. Only lowercase characters (a-z), digits (0-9), and any of the characters _, $, (, ), +, -, and / are allowed. Must begin with a letter."));

    Future<JsonObject> result = client.createDb("_invalid_db");

    result.onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Error creating database: illegal_database_name - Name: '_invalid_db'. Only lowercase characters (a-z), digits (0-9), and any of the characters _, $, (, ), +, -, and / are allowed. Must begin with a letter.", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbUnauthorized(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.failedFuture(new Exception("Error creating database: unauthorized - Name or password is incorrect.")));

    CouchdbClient unauthorizedClient = CouchdbClient.create(vertx, mockWebClient, new UsernamePasswordCredentials("invalid_user", "invalid_password"));

    unauthorizedClient.createDb("unauthorized_db").onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Error creating database: unauthorized - Name or password is incorrect.", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        fail("Expected failure for unauthorized user but succeeded.");
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

}
