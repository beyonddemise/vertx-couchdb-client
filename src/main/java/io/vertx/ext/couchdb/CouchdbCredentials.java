package io.vertx.ext.couchdb;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.codegen.json.annotations.JsonGen;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;
import io.vertx.ext.auth.authentication.UsernamePasswordCredentials;
import io.vertx.ext.auth.authentication.TokenCredentials;

@DataObject
@JsonGen
public class CouchdbCredentials {

  private String username;
  private String password;
  private String token;

  public CouchdbCredentials() {
  }

  public CouchdbCredentials(String username, String password) {
    this.username = username;
    this.password = password;
  }

  public CouchdbCredentials(String token) {
    this.token = token;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getToken() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }

  public Credentials toAuthCredentials() {
    if (username != null && password != null) {
      return new UsernamePasswordCredentials(username, password);
    } else if (token != null) {
      return new TokenCredentials(token);
    } else {
      throw new IllegalArgumentException("Invalid credentials: either username and password, or token must be provided.");
    }
  }

  public JsonObject toJson() {
    JsonObject json = new JsonObject();
    CouchdbCredentialsConverter.toJson(this, json);
    return json;
  }

  public static CouchdbCredentials fromJson(JsonObject json) {
    CouchdbCredentials credentials = new CouchdbCredentials();
    CouchdbCredentialsConverter.fromJson(json, credentials);
    return credentials;
  }
}
