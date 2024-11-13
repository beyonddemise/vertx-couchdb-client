package io.vertx.ext.couchdb.database.security;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;


public class DBSecurity {

  static final String SYSTEM_ADMIN_ROLE = "_admin";
  static final String NAMES = "names";
  static final String ROLES = "roles";
  static final String ADMINS = " admins";
  static final String MEMBERS = "members";

  // 4 distince security groups
  private final Set<String> adminNames = new HashSet<>();
  private final Set<String> adminRoles = new HashSet<>();
  private final Set<String> memberNames = new HashSet<>();
  private final Set<String> memberRoles = new HashSet<>();

  public DBSecurity clear() {
    this.clearAdminNames();
    this.clearAdminRoles();
    this.clearMemberNames();
    this.clearMemberRoles();
    return this;
  }

  public DBSecurity clearAdminNames() {
    this.adminNames.clear();
    return this;
  }

  public DBSecurity clearAdminRoles() {
    this.adminRoles.clear();
    return this;
  }

  public DBSecurity clearMemberNames() {
    this.memberNames.clear();
    return this;
  }

  public DBSecurity clearMemberRoles() {
    this.memberRoles.clear();
    return this;
  }

  public DBSecurity removeAdminName(final String moriturus) {
    Objects.requireNonNull(moriturus);
    this.adminNames.remove(moriturus);
    return this;
  }

  public DBSecurity removeAdminRole(final String moriturus) {
    Objects.requireNonNull(moriturus);
    this.adminRoles.remove(moriturus);
    return this;
  }

  public DBSecurity removeMemberName(final String moriturus) {
    Objects.requireNonNull(moriturus);
    this.memberNames.remove(moriturus);
    return this;
  }

  public DBSecurity removeMemberRole(final String moriturus) {
    Objects.requireNonNull(moriturus);
    this.memberRoles.remove(moriturus);
    return this;
  }

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
    newNames.forEach(this::addAdminName);
    return this;
  }

  public DBSecurity addAdminRoles(Collection<String> newRoles) {
    Objects.requireNonNull(newRoles);
    newRoles.forEach(this::addAdminRole);
    return this;
  }

  public DBSecurity addMemberName(String newName) {
    Objects.requireNonNull(newName);
    this.memberNames.add(newName);
    return this;
  }

  public DBSecurity addMemberNames(Collection<String> newNames) {
    Objects.requireNonNull(newNames);
    newNames.forEach(this::addMemberName);
    return this;
  }

  public DBSecurity addMemberRole(String newRole) {
    Objects.requireNonNull(newRole);
    this.memberRoles.add(newRole);
    return this;
  }

  public DBSecurity addMemberRoles(Collection<String> newRoles) {
    Objects.requireNonNull(newRoles);
    newRoles.forEach(this::addMemberRole);
    return this;
  }


  public Set<String> getAdminNames() {
    return this.adminNames;
  }

  public Set<String> getAdminRoles() {
    // enforces that the system admin role is always present
    this.adminRoles.add(SYSTEM_ADMIN_ROLE);
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

    resAdmins.put(NAMES, dbSec.getAdminNames());
    resAdmins.put(ROLES, dbSec.getAdminRoles());
    resMembers.put(NAMES, dbSec.getMemberNames());
    resMembers.put(ROLES, dbSec.getMemberRoles());

    result.put(ADMINS, resAdmins);
    result.put(MEMBERS, resMembers);

    return result;
  }

  public static DBSecurity fromJson(JsonObject dbSecObject) {
    Objects.requireNonNull(dbSecObject);

    DBSecurity dbSec = new DBSecurity();

    dbSec.addAdminNames(dbSecObject.getJsonObject(ADMINS, new JsonObject())
        .getJsonArray(NAMES, new JsonArray()).stream()
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .filter(x -> x.trim().length() > 0)
        .collect(Collectors.toList()));

    dbSec.addAdminRoles(dbSecObject.getJsonObject(ADMINS, new JsonObject())
        .getJsonArray(ROLES, new JsonArray()).stream()
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .filter(x -> x.trim().length() > 0)
        .collect(Collectors.toList()));

    dbSec.addMemberNames(dbSecObject.getJsonObject(MEMBERS, new JsonObject())
        .getJsonArray(NAMES, new JsonArray()).stream()
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .filter(x -> x.trim().length() > 0)
        .collect(Collectors.toList()));

    dbSec.addMemberRoles(dbSecObject.getJsonObject(MEMBERS, new JsonObject())
        .getJsonArray(ROLES, new JsonArray()).stream()
        .filter(Objects::nonNull)
        .map(String::valueOf)
        .filter(x -> x.trim().length() > 0)
        .collect(Collectors.toList()));

    return dbSec;
  }
}
