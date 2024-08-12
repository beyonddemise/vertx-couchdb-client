package io.vertx.ext.couchdb;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.impl.CouchdbClientImpl;
import io.vertx.ext.couchdb.utils.UnitTest;
import io.vertx.ext.web.client.HttpRequest;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.CREATED;
import static io.netty.handler.codec.http.HttpResponseStatus.PRECONDITION_FAILED;
import static io.netty.handler.codec.http.HttpResponseStatus.UNAUTHORIZED;
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
  void setUp() throws Exception {
    Vertx vertx = Vertx.vertx();
    client = CouchdbClient.create(vertx, couchdbClientOptions());

    CouchdbClientImpl clientImpl = (CouchdbClientImpl) client;
    Field clientField = CouchdbClientImpl.class.getDeclaredField("client");
    clientField.setAccessible(true);
    clientField.set(clientImpl, mockWebClient);
  }

  @Test
  void testCreate() {
    assertNotNull(client);
  }

  @Test
  void testStatus(VertxTestContext testContext) throws InterruptedException {
    Future<JsonObject> future = client.status();
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject status = ar.result();
        System.out.println("Full status response: " + status.encodePrettily());
        try {
          assertNotNull(status);
          assertEquals("Welcome", status.getString("couchdb"));
          testContext.completeNow();
        } catch (AssertionError e) {
          testContext.failNow(e);
        }
      } else {
        System.out.println("Failed to get status: " + ar.cause());
        testContext.failNow(ar.cause());
      }
    });
    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testActiveTasks() {
    Future<JsonArray> future = client.activeTasks();
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonArray tasks = ar.result();
        assertNotNull(tasks);
      } else {
        fail("Failed to get active tasks");
      }
    });
  }

  @Test
  void testAllDbs() {
    Future<JsonArray> future = client.allDbs();
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonArray dbs = ar.result();
        assertNotNull(dbs);
      } else {
        fail("Failed to get all databases");
      }
    });
  }

  @Test
  void testCreateDbSuccess(VertxTestContext testContext) throws InterruptedException {
    JsonObject options = new JsonObject().put("db_name", "new_db");

    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(CREATED.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("ok", true));

    Future<JsonObject> result = client.createDb(options);

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
    JsonObject options = new JsonObject().put("db_name", "existing_db");

    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(PRECONDITION_FAILED.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("error", "file_exists").put("reason", "The database could not be created, the file already exists."));

    Future<JsonObject> result = client.createDb(options);

    result.onComplete(ar -> {
      if (ar.succeeded()) {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      } else {
        assertEquals("Error creating database: file_exists - The database could not be created, the file already exists.", ar.cause().getMessage());
        testContext.completeNow();
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbInvalidDatabaseName(VertxTestContext testContext) throws InterruptedException {
    JsonObject options = new JsonObject().put("db_name", "_invalid_db");

    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(BAD_REQUEST.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("error", "illegal_database_name").put("reason", "Name: '_invalid_db'. Only lowercase characters (a-z), digits (0-9), and any of the characters _, $, (, ), +, -, and / are allowed. Must begin with a letter."));

    Future<JsonObject> result = client.createDb(options);

    result.onComplete(ar -> {
      if (ar.succeeded()) {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      } else {
        assertEquals("Error creating database: illegal_database_name - Name: '_invalid_db'. Only lowercase characters (a-z), digits (0-9), and any of the characters _, $, (, ), +, -, and / are allowed. Must begin with a letter.", ar.cause().getMessage());
        testContext.completeNow();
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbUnauthorized(VertxTestContext testContext) throws InterruptedException {
    JsonObject options = new JsonObject().put("db_name", "new_db");

    when(mockWebClient.put(anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.addQueryParam(anyString(), anyString())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.authentication(any())).thenReturn(mockHttpRequest);
    when(mockHttpRequest.send()).thenReturn(Future.succeededFuture(mockHttpResponse));
    when(mockHttpResponse.statusCode()).thenReturn(UNAUTHORIZED.code());
    when(mockHttpResponse.bodyAsJsonObject()).thenReturn(new JsonObject().put("error", "unauthorized").put("reason", "CouchDB Server Administrator privileges required."));

    Future<JsonObject> result = client.createDb(options);

    result.onComplete(ar -> {
      if (ar.succeeded()) {
        testContext.failNow(new AssertionError("Expected to fail, but succeeded"));
      } else {
        assertEquals("Error creating database: unauthorized - CouchDB Server Administrator privileges required.", ar.cause().getMessage());
        testContext.completeNow();
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  protected CouchdbClientOptions couchdbClientOptions() {
    CouchdbClientOptions options = new CouchdbClientOptions()
      .setHost("localhost")
      .setPort(5984)
      .setCredentials(new CouchdbCredentials("admin", "password"))
      .setDatabase("my_database");
    return options;
  }
}
