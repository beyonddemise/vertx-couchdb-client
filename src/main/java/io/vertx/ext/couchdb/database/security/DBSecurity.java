package io.vertx.ext.couchdb.database.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class DBSecurity {

  // might be good to change to map and list collection native
  private Set<String> adminNames = new HashSet<>();
  private Set<String> adminRoles = new HashSet<>();
  private Set<String> memberNames = new HashSet<>();
  private Set<String> memberRoles = new HashSet<>();

  public DBSecurity addAdminName(String newName) {
    Objects.requireNonNull(newName);
    this.adminNames.add(newName);
    return this;
  }

  public DBSecurity addAdminRole(String newRole) {
    Objects.requireNonNull(newRole);
    this.adminRoles.add(newRole);
    return this;
  }

  public DBSecurity addAdminNames(Collection<String> newNames) {
    Objects.requireNonNull(newNames);
    this.adminNames.addAll(newNames);
    return this;
  }

  public DBSecurity addAdminRoles(Collection<String> newRoles) {
    Objects.requireNonNull(newRoles);
    this.adminRoles.addAll(newRoles);
    return this;
  }

  public DBSecurity addMemberName(String newName) {
    Objects.requireNonNull(newName);
    this.memberNames.add(newName);
    return this;
  }

  public DBSecurity addMemberNames(Collection<String> newNames) {
    Objects.requireNonNull(newNames);
    this.memberRoles.addAll(newNames);
    return this;
  }

  public DBSecurity addMemberRole(String newRole) {
    Objects.requireNonNull(newRole);
    this.memberNames.add(newRole);
    return this;
  }

  public DBSecurity addMemberRoles(Collection<String> newRoles) {
    Objects.requireNonNull(newRoles);
    this.memberRoles.addAll(newRoles);
    return this;
  }


  public Set<String> getAdminNames() {
    return this.adminNames;
  }

  public Set<String> getAdminRoles() {
    return this.adminRoles;
  }

  public Set<String> getMemberNames() {
    return this.memberNames;
  }

  public Set<String> getMemberRoles() {
    return this.memberRoles;
  }

  public static JsonObject toJson(DBSecurity dbSec) {
    JsonObject result = new JsonObject();
    JsonObject resAdmins = new JsonObject();
    JsonObject resMembers = new JsonObject();

    resAdmins.put("names", dbSec.getAdminNames());
    resAdmins.put("roles", dbSec.getAdminRoles());
    resMembers.put("names", dbSec.getMemberNames());
    resMembers.put("roles", dbSec.getMemberRoles());

    result.put("admins", resAdmins);
    result.put("members", resMembers);

    return result;
  }

  public static DBSecurity fromJson(JsonObject dbSecObject) {
    DBSecurity dbSec = new DBSecurity();
    dbSec.addAdminNames(dbSecObject.getJsonObject("admins", new JsonObject())
        .getJsonArray("names", new JsonArray()).stream().map(String::valueOf)
        .collect(Collectors.toList()));
    dbSec.addAdminRoles(dbSecObject.getJsonObject("admins", new JsonObject())
        .getJsonArray("roles", new JsonArray()).stream().map(String::valueOf)
        .collect(Collectors.toList()));
    dbSec.addMemberNames(dbSecObject.getJsonObject("members", new JsonObject())
        .getJsonArray("names", new JsonArray()).stream().map(String::valueOf)
        .collect(Collectors.toList()));
    dbSec.addMemberRoles(dbSecObject.getJsonObject("members", new JsonObject())
        .getJsonArray("roles", new JsonArray()).stream().map(String::valueOf)
        .collect(Collectors.toList()));

    return dbSec;
  }
}
