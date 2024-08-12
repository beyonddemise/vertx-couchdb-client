package io.vertx.ext.couchdb;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.utils.IntegrationTest;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@IntegrationTest
@Testcontainers
public class CouchdbClientIT {

  @Container
  private static final GenericContainer<?> couchdbContainer = new GenericContainer<>(DockerImageName.parse("couchdb:3.3.3"))
    .withExposedPorts(5984)
    .withEnv("COUCHDB_USER", "admin")
    .withEnv("COUCHDB_PASSWORD", "password");

  private static CouchdbClient client;
  private static Vertx vertx;

  @BeforeAll
  static void setup() {
    vertx = Vertx.vertx();

    client = CouchdbClient.create(vertx, couchdbClientOptions());
  }

  @AfterAll
  static void tearDown() {
    if (vertx != null) {
      vertx.close();
    }
    if (couchdbContainer != null) {
      couchdbContainer.stop();
    }
  }

  @Test
  void testCreateDbSuccess(VertxTestContext testContext) throws InterruptedException {
    JsonObject options = new JsonObject().put("db_name", "new_db");

    Future<JsonObject> result = client.createDb(options);

    result.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject dbResult = ar.result();
        assertNotNull(dbResult);
        assertEquals(true, dbResult.getBoolean("ok"));
        testContext.completeNow();
      } else {
        fail("Failed to create database: " + ar.cause().getMessage());
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbAlreadyExists(VertxTestContext testContext) throws InterruptedException {
    JsonObject options = new JsonObject().put("db_name", "existing_db");

    client.createDb(options).onComplete(ar1 -> {
      if (ar1.succeeded()) {
        client.createDb(options).onComplete(ar2 -> {
          if (ar2.failed()) {
            assertEquals("Error creating database: file_exists - The database could not be created, the file already exists.",
              ar2.cause().getMessage());
            testContext.completeNow();
          } else {
            fail("Expected failure for already existing database but succeeded.");
          }
        });
      } else {
        fail("Initial database creation failed unexpectedly: " + ar1.cause().getMessage());
      }
    });

    testContext.awaitCompletion(10, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbInvalidName(VertxTestContext testContext) throws InterruptedException {
    JsonObject options = new JsonObject().put("db_name", "_invalid_db");

    client.createDb(options).onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Error creating database: illegal_database_name - Name: '_invalid_db'. Only lowercase characters (a-z), digits (0-9), and any of the characters _, $, (, ), +, -, and / are allowed. Must begin with a letter.", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        fail("Expected failure for invalid database name but succeeded.");
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbUnauthorized(VertxTestContext testContext) throws InterruptedException {
    CouchdbClientOptions options = couchdbClientOptions()
      .setCredentials(new CouchdbCredentials("invalid_user", "invalid_password"));
    CouchdbClient unauthorizedClient = CouchdbClient.create(vertx, options);

    JsonObject dbOptions = new JsonObject().put("db_name", "unauthorized_db");

    unauthorizedClient.createDb(dbOptions).onComplete(ar -> {
      if (ar.failed()) {
        assertEquals("Error creating database: unauthorized - Name or password is incorrect.", ar.cause().getMessage());
        testContext.completeNow();
      } else {
        fail("Expected failure for unauthorized user but succeeded.");
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }



  protected static CouchdbClientOptions couchdbClientOptions() {
    return new CouchdbClientOptions()
      .setHost(couchdbContainer.getHost())
      .setPort(couchdbContainer.getMappedPort(5984))
      .setCredentials(new CouchdbCredentials("admin", "password"));
  }
}
