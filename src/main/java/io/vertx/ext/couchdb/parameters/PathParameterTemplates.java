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

import io.vertx.uritemplate.UriTemplate;
import io.vertx.uritemplate.Variables;

public class PathParameterTemplates {

  public static final String QUERY = "{?query*}";

  public static UriTemplate templateWithQueryParams(String rawTtemplate, Variables variables) {
    Variables actualVariables = variables != null ? variables : Variables.variables();
    UriTemplate temp = UriTemplate.of(rawTtemplate);
    return UriTemplate.of(temp.expandToString(actualVariables) + QUERY);
  }

  public static UriTemplate database(String dbName) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    return templateWithQueryParams("/{database}", variables);
  }

  public static UriTemplate databaseDocumentId(String dbName, String documentId) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    variables.set("documentId", documentId);
    return templateWithQueryParams("/{database}/{documentId}", variables);
  }


  public static UriTemplate databaseSecurity(String dbName) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    return templateWithQueryParams("/{database}/_security", variables);
  }

  public static UriTemplate databaseDesignDoc(String dbName, String designDocName) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    variables.set("designDoc", designDocName);
    return templateWithQueryParams("/{database}/_design/{designDoc}", variables);
  }

  public static UriTemplate attachment(String dbName, String documentId, String attachmentName) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    variables.set("documentId", documentId);
    variables.set("attachmentName", attachmentName);
    return templateWithQueryParams("/{database}/{documentId}/{attachmentName}", variables);
  }

  private PathParameterTemplates() {
    // Private constructor to prevent instantiation
  }

}
