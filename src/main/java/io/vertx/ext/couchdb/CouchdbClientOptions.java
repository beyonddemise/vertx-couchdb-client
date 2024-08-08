package io.vertx.ext.couchdb;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.WebClientOptions;

@DataObject
@JsonGen
public class CouchdbClientOptions {

  /**
   * Default port for connecting with Couchdb server.
   */
  public static final int DEFAULT_PORT = 5984;

  /**
   * Default host for connecting with Couchdb server.
   */
  public static final String DEFAULT_HOST = "localhost";

  private String host;
  private int port;
  private CouchdbCredentials credentials;
  private String database;
  private WebClientOptions webClientOptions;

  public CouchdbClientOptions() {
    this.host = DEFAULT_HOST;
    this.port = DEFAULT_PORT;
    this.webClientOptions = new WebClientOptions()
      .setDefaultHost(DEFAULT_HOST)
      .setDefaultPort(DEFAULT_PORT);
  }

  public CouchdbClientOptions(CouchdbClientOptions other) {
    this.host = other.host;
    this.port = other.port;
    this.credentials = other.credentials;
    this.database = other.database;
    this.webClientOptions = other.webClientOptions != null ? other.webClientOptions : new WebClientOptions()
      .setDefaultHost(other.host)
      .setDefaultPort(other.port);
  }

  public CouchdbClientOptions(JsonObject json) {
    this();
    CouchdbClientOptionsConverter.fromJson(json, this);
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CouchdbClientOptionsConverter.toJson(this, json);
    return json;
  }

  public String getHost() {
    return host;
  }

  public CouchdbClientOptions setHost(String host) {
    this.host = host;
    if (this.webClientOptions != null) {
      this.webClientOptions.setDefaultHost(host);
    }
    return this;
  }

  public int getPort() {
    return port;
  }

  public CouchdbClientOptions setPort(int port) {
    this.port = port;
    if (this.webClientOptions != null) {
      this.webClientOptions.setDefaultPort(port);
    }
    return this;
  }

  public CouchdbCredentials getCredentials() {
    return credentials;
  }

  public CouchdbClientOptions setCredentials(CouchdbCredentials credentials) {
    this.credentials = credentials;
    return this;
  }

  public String getDatabase() {
    return database;
  }

  public CouchdbClientOptions setDatabase(String database) {
    this.database = database;
    return this;
  }

  public WebClientOptions getWebClientOptions() {
    return webClientOptions;
  }

  public CouchdbClientOptions setWebClientOptions(WebClientOptions webClientOptions) {
    this.webClientOptions = webClientOptions;
    return this;
  }
}
