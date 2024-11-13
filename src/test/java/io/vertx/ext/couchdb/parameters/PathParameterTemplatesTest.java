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
package io.vertx.ext.couchdb.parameters;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

import io.vertx.uritemplate.UriTemplate;
import io.vertx.uritemplate.Variables;

public class PathParameterTemplatesTest {
  @Test
  void testDatabase() {
    UriTemplate template = PathParameterTemplates.database("test");
    assertEquals("/test", template.expandToString(Variables.variables()));
  }

  @Test
  void testDatabasewithParams() {
    UriTemplate template = PathParameterTemplates.database("test2");
    DbCreateParams params = new DbCreateParams().replicas(7);
    Variables variables = Variables.variables();
    variables.set("query", params.forTemplate());
    assertEquals("/test2?n=7", template.expandToString(variables));
  }

  @Test
  void testDatabaseDocumentId() {
    UriTemplate template = PathParameterTemplates.databaseDocumentId("test", "123");
    assertEquals("/test/123", template.expandToString(Variables.variables()));
  }

}
