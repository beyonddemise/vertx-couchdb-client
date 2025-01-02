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


  /**
   * Creates a URI template for accessing the security endpoint of a specified database.
   *
   * @param dbName the name of the database to access security settings for
   * @return a UriTemplate object representing the security endpoint path with the database name
   * @throws IllegalArgumentException if dbName is null or empty
   * @see UriTemplate
   */
  public static UriTemplate databaseSecurity(String dbName) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    return templateWithQueryParams("/{database}/_security", variables);
  }

  /**
   * Creates a URI template for accessing a design document in a CouchDB database.
   *
   * @param dbName        the name of the database
   * @param designDocName the name of the design document
   * @return a UriTemplate object representing the path to the design document
   * @throws IllegalArgumentException if dbName or designDocName is null or empty
   * @see UriTemplate
   */
  public static UriTemplate databaseDesignDoc(String dbName, String designDocName) {
    Variables variables = Variables.variables();
    variables.set("database", dbName);
    variables.set("designDoc", designDocName);
    return templateWithQueryParams("/{database}/_design/{designDoc}", variables);
  }

  /**
   * Creates a URI template for accessing a document attachment in a CouchDB database.
   *
   * @param dbName          the name of the database containing the document
   * @param documentId      the ID of the document containing the attachment
   * @param attachmentName  the name of the attachment to access
   * @return               a UriTemplate object representing the path to the attachment
   * @throws IllegalArgumentException if any of the parameters are null or empty
   * @see                 UriTemplate
   */
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
