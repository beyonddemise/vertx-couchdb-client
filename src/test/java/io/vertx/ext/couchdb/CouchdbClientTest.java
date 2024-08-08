package io.vertx.ext.couchdb;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.utils.UnitTest;
import io.vertx.ext.web.client.WebClientOptions;

@UnitTest
class CouchdbClientTest {

  Vertx vertx;
  WebClientOptions options;
  CouchdbClient client = null;

  @BeforeEach
  void beforeEach() {
    this.vertx = Vertx.vertx();

    CouchdbClientOptions options = new CouchdbClientOptions()
      .setHost("localhost")
      .setPort(5984)
      .setCredentials(new CouchdbCredentials("admin", "password"))
      .setDatabase("my_database");

    this.client = CouchdbClient.create(vertx, options);
  }


  @Test
  void testCreate() {
    assertNotNull(client);
  }

  @Test
  void testStatus() {
    Future<JsonObject> future = client.status();
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject status = ar.result();
        assertNotNull(status);
        assertEquals("Welcome", status.getString("couchdb"));
      } else {
        fail("Failed to get status");
      }
    });
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
}
