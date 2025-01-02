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

package io.vertx.ext.couchdb.database.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

class DBSecurityTest {

  private DBSecurity dbSecurity;

  @BeforeEach
  void setUp() {
    dbSecurity = new DBSecurity();
  }

  @Test
  void testClear() {
    dbSecurity.addAdminName("admin1");
    dbSecurity.addAdminRole("role1");
    dbSecurity.addMemberName("member1");
    dbSecurity.addMemberRole("role2");

    dbSecurity.clear();

    assertTrue(dbSecurity.getAdminNames().isEmpty());
    // Admin roles always contains SYSTEM_ADMIN_ROLE
    assertEquals(1, dbSecurity.getAdminRoles().size());
    assertTrue(dbSecurity.getMemberNames().isEmpty());
    assertTrue(dbSecurity.getMemberRoles().isEmpty());
  }

  @Test
  void testClearAdminNames() {
    dbSecurity.addAdminName("admin1");
    dbSecurity.clearAdminNames();
    assertTrue(dbSecurity.getAdminNames().isEmpty());
  }

  @Test
  void testClearAdminRoles() {
    dbSecurity.addAdminRole("role1");
    dbSecurity.clearAdminRoles();
    // Admin roles always contains SYSTEM_ADMIN_ROLE
    assertEquals(1, dbSecurity.getAdminRoles().size());
  }

  @Test
  void testClearMemberNames() {
    dbSecurity.addMemberName("member1");
    dbSecurity.clearMemberNames();
    assertTrue(dbSecurity.getMemberNames().isEmpty());
  }

  @Test
  void testClearMemberRoles() {
    dbSecurity.addMemberRole("role1");
    dbSecurity.clearMemberRoles();
    assertTrue(dbSecurity.getMemberRoles().isEmpty());
  }

  @Test
  void testRemoveAdminName() {
    dbSecurity.addAdminName("admin1");
    dbSecurity.removeAdminName("admin1");
    assertFalse(dbSecurity.getAdminNames().contains("admin1"));
  }

  @Test
  void testRemoveAdminRole() {
    dbSecurity.addAdminRole("role1");
    dbSecurity.removeAdminRole("role1");
    assertFalse(dbSecurity.getAdminRoles().contains("role1"));
  }

  @Test
  void testRemoveMemberName() {
    dbSecurity.addMemberName("member1");
    dbSecurity.removeMemberName("member1");
    assertFalse(dbSecurity.getMemberNames().contains("member1"));
  }

  @Test
  void testRemoveMemberRole() {
    dbSecurity.addMemberRole("role1");
    dbSecurity.removeMemberRole("role1");
    assertFalse(dbSecurity.getMemberRoles().contains("role1"));
  }

  @Test
  void testAddAdminName() {
    dbSecurity.addAdminName("admin1");
    assertTrue(dbSecurity.getAdminNames().contains("admin1"));
  }

  @Test
  void testAddAdminRole() {
    dbSecurity.addAdminRole("role1");
    assertTrue(dbSecurity.getAdminRoles().contains("role1"));
  }

  @Test
  void testAddAdminNames() {
    dbSecurity.addAdminNames(Arrays.asList("admin1", "admin2"));
    assertTrue(dbSecurity.getAdminNames().containsAll(Arrays.asList("admin1", "admin2")));
  }

  @Test
  void testAddAdminRoles() {
    dbSecurity.addAdminRoles(Arrays.asList("role1", "role2"));
    assertTrue(dbSecurity.getAdminRoles().containsAll(Arrays.asList("role1", "role2")));
  }

  @Test
  void testAddMemberName() {
    dbSecurity.addMemberName("member1");
    assertTrue(dbSecurity.getMemberNames().contains("member1"));
  }

  @Test
  void testAddMemberNames() {
    dbSecurity.addMemberNames(Arrays.asList("member1", "member2"));
    assertTrue(dbSecurity.getMemberNames().containsAll(Arrays.asList("member1", "member2")));
  }

  @Test
  void testAddMemberRole() {
    dbSecurity.addMemberRole("role1");
    assertTrue(dbSecurity.getMemberRoles().contains("role1"));
  }

  @Test
  void testAddMemberRoles() {
    dbSecurity.addMemberRoles(Arrays.asList("role1", "role2"));
    assertTrue(dbSecurity.getMemberRoles().containsAll(Arrays.asList("role1", "role2")));
  }

  @Test
  void testGetAdminNames() {
    dbSecurity.addAdminName("admin1");
    Set<String> adminNames = dbSecurity.getAdminNames();
    assertEquals(new HashSet<>(Collections.singletonList("admin1")), adminNames);
  }

  @Test
  void testGetAdminRoles() {
    dbSecurity.addAdminRole("role1");
    Set<String> adminRoles = dbSecurity.getAdminRoles();
    assertTrue(adminRoles.contains("role1"));
    assertTrue(adminRoles.contains(DBSecurity.SYSTEM_ADMIN_ROLE));
  }

  @Test
  void testGetMemberNames() {
    dbSecurity.addMemberName("member1");
    Set<String> memberNames = dbSecurity.getMemberNames();
    assertEquals(new HashSet<>(Collections.singletonList("member1")), memberNames);
  }

  @Test
  void testGetMemberRoles() {
    dbSecurity.addMemberRole("role1");
    Set<String> memberRoles = dbSecurity.getMemberRoles();
    assertEquals(new HashSet<>(Collections.singletonList("role1")), memberRoles);
  }

  @Test
  void testToJson() {
    dbSecurity.addAdminName("admin1");
    dbSecurity.addAdminRole("role1");
    dbSecurity.addMemberName("member1");
    dbSecurity.addMemberRole("role2");

    JsonObject json = dbSecurity.toJson();

    JsonArray adminNames = json.getJsonObject(DBSecurity.ADMINS).getJsonArray(DBSecurity.NAMES);
    JsonArray adminRoles = json.getJsonObject(DBSecurity.ADMINS).getJsonArray(DBSecurity.ROLES);
    JsonArray memberNames = json.getJsonObject(DBSecurity.MEMBERS).getJsonArray(DBSecurity.NAMES);
    JsonArray memberRoles = json.getJsonObject(DBSecurity.MEMBERS).getJsonArray(DBSecurity.ROLES);

    assertEquals("admin1", adminNames.getString(0));
    assertEquals("role1", adminRoles.getString(0));
    assertEquals("member1", memberNames.getString(0));
    assertEquals("role2", memberRoles.getString(0));

    assertFalse(adminNames.contains("role1"));
    assertFalse(adminRoles.contains("admin1"));
    assertFalse(memberNames.contains("role2"));
    assertFalse(memberRoles.contains("member1"));

    assertFalse(adminNames.contains("member1"));
    assertFalse(adminRoles.contains("role2"));
    assertFalse(memberNames.contains("admin1"));
    assertFalse(memberRoles.contains("role1"));

  }

  @Test
  void testFromJson() {
    JsonObject json = new JsonObject()
        .put(DBSecurity.ADMINS, new JsonObject()
            .put(DBSecurity.NAMES, new JsonArray().add("admin1"))
            .put(DBSecurity.ROLES, new JsonArray().add("role1")))
        .put(DBSecurity.MEMBERS, new JsonObject()
            .put(DBSecurity.NAMES, new JsonArray().add("member1"))
            .put(DBSecurity.ROLES, new JsonArray().add("role2")));

    DBSecurity localDbSecurity = DBSecurity.fromJson(json);

    assertTrue(localDbSecurity.getAdminNames().contains("admin1"));
    assertTrue(localDbSecurity.getAdminRoles().contains("role1"));
    assertTrue(localDbSecurity.getAdminRoles().contains(DBSecurity.SYSTEM_ADMIN_ROLE));
    assertTrue(localDbSecurity.getMemberNames().contains("member1"));
    assertTrue(localDbSecurity.getMemberRoles().contains("role2"));
  }
}
