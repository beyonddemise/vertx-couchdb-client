package io.vertx.ext.couchdb;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.couchdb.testannotations.IntegrationTest;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;
import io.vertx.junit5.VertxTestContext;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
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

  private static final int COUCHDB_PORT = 5984;

  @Container
  private static final GenericContainer<?> couchdbContainer = new GenericContainer<>(DockerImageName.parse("couchdb:3.3.3"))
    .withExposedPorts(COUCHDB_PORT)
    .withEnv("COUCHDB_USER", "admin")
    .withEnv("COUCHDB_PASSWORD", "password")
    .waitingFor(Wait.forListeningPort());

  private static CouchdbClient client;

  @BeforeAll
  static void setup(Vertx vertx) {
    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost(couchdbContainer.getHost())
      .setDefaultPort(couchdbContainer.getMappedPort(COUCHDB_PORT)));

    client = CouchdbClient.create(vertx, webClient, new UsernamePasswordCredentials("admin", "password"));

  }

  @AfterAll
  static void tearDown(Vertx vertx) {
    if (vertx != null) {
      vertx.close();
    }
    if (couchdbContainer != null) {
      couchdbContainer.stop();
    }
  }

  @Test
  void testCreateDbSuccess(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    Future<JsonObject> result = client.createDb("new_db");

    result.onComplete(ar -> {
      if (ar.succeeded()) {
        JsonObject dbResult = ar.result();
        assertNotNull(dbResult);
        assertEquals(true, dbResult.getBoolean("ok"));
        testContext.completeNow();
      } else {
        // Print the cause of the failure for debugging
        System.err.println("Failed to create database: " + ar.cause().getMessage());
        ar.cause().printStackTrace();
        fail("Failed to create database: " + ar.cause().getMessage());
      }
    });

    testContext.awaitCompletion(5, TimeUnit.SECONDS);
  }

  @Test
  void testCreateDbAlreadyExists(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    client.createDb("existing_db").onComplete(ar1 -> {
      if (ar1.succeeded()) {
        client.createDb("existing_db").onComplete(ar2 -> {
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
  void testCreateDbInvalidName(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    client.createDb("_invalid_db").onComplete(ar -> {
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
  void testCreateDbUnauthorized(Vertx vertx, VertxTestContext testContext) throws InterruptedException {
    WebClient webClient = WebClient.create(vertx, new WebClientOptions()
      .setDefaultHost(couchdbContainer.getHost())
      .setDefaultPort(couchdbContainer.getMappedPort(COUCHDB_PORT)));

    CouchdbClient unauthorizedClient = CouchdbClient.create(vertx, webClient,
      new UsernamePasswordCredentials("invalid_user", "invalid_password"));

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


  protected static String getCouchdbHost() {
    return couchdbContainer.getHost();
  }

  protected static int getCouchdbPort() {
    return couchdbContainer.getMappedPort(COUCHDB_PORT);
  }
}
