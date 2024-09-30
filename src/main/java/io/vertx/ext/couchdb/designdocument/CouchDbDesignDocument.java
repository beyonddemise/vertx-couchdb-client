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

package io.vertx.ext.couchdb.designdocument;

import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import io.vertx.core.json.JsonArray;

public interface CouchDbDesignDocument {

  /**
   * Retrieves all design documents from the database.
   *
   * @param dbName The name of the database.
   * @return Future containing a JSON array with all design documents in the specified database.
   */
  Future<JsonArray> getAllDesignDocuments(String dbName);

  /**
   * Creates a new index in the specified design document.
   *
   * @param dbName The name of the database.
   * @param designDocName The name of the design document.
   * @param indexName The name of the index.
   * @param indexDefinition The JSON object defining the index.
   * @return Future with the result of the index creation.
   */
  Future<JsonObject> createIndex(String dbName, String designDocName, String indexName, JsonObject indexDefinition);

  /**
   * Updates an existing index in the specified design document.
   *
   * @param dbName The name of the database.
   * @param designDocName The name of the design document.
   * @param indexName The name of the index.
   * @param indexDefinition The JSON object with updated index definition.
   * @return Future with the result of the index update.
   */
  Future<JsonObject> updateIndex(String dbName, String designDocName, String indexName, JsonObject indexDefinition);

  /**
   * Retrieves a specific design document by its name.
   *
   * @param dbName The name of the database.
   * @param designDocName The name of the design document.
   * @return Future containing the design document as a JSON object.
   */
  Future<JsonObject> getDesignDocument(String dbName, String designDocName);

  /**
   * Deletes a specific design document by its name.
   *
   * @param dbName The name of the database.
   * @param designDocName The name of the design document to delete.
   * @return Future with the result of the delete operation.
   */
  Future<JsonObject> deleteDesignDocument(String dbName, String designDocName);
}
