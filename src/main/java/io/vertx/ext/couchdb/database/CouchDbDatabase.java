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
package io.vertx.ext.couchdb.database;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.CouchdbClient;
import io.vertx.ext.couchdb.database.designdoc.DBDesignDoc;
import io.vertx.ext.couchdb.database.impl.CouchDbDatabaseImpl;
import io.vertx.ext.couchdb.database.security.DBSecurity;
import io.vertx.ext.couchdb.parameters.DocumentGetParams;
import io.vertx.ext.couchdb.streams.CouchDbStream;

public interface CouchDbDatabase {

  static Future<CouchDbDatabase> create(CouchdbClient client, String databaseName) {
    return CouchDbDatabaseImpl.create(client, databaseName);
  }

  String name();

  Future<JsonObject> status();

  CouchDbStream stream(/* TODO: replace with specific object */ JsonObject options);

  Future<JsonObject> createDocument(String docId, JsonObject document);

  Future<JsonObject> updateDocument(String docId, String rev, JsonObject document);

  /**
 * Retrieves a document from the database asynchronously.
 *
 * @param docId    the unique identifier of the document to retrieve
 * @param options  parameters to customize the get operation (e.g., consistency, timeout, projections)
 * @return         a Future containing the document as a JsonObject if found, or null if not found
 * @throws IllegalArgumentException if docId is null or empty
 * @throws DocumentServiceException if there's an error accessing the database
 * @see DocumentGetParams
 */
Future<JsonObject> getDocument(String docId, DocumentGetParams options);

  /**
   * Retrieves a document by its ID asynchronously.
   *
   * @param docId the unique identifier of the document to retrieve
   * @return a Future containing the document as a JsonObject if found, or null if not found
   * @see #getDocument(String, JsonObject)
   */
  default Future<JsonObject> getDocument(String docId) {
    return this.getDocument(docId, null);
  };

  Future<Buffer> getDocumentAttachment(String docId, String attachementName,
      String rev);

  default Future<Buffer> getDocumentAttachment(String docId, String attachementName) {
    return this.getDocumentAttachment(docId, attachementName, null);
  }

  default Future<JsonObject> deleteDocument(String docId, String rev) {
    return this.deleteDocument(docId, rev, false);
  }

  /**
 * Deletes a document from the database.
 *
 * @param docId  the unique identifier of the document to delete
 * @param rev    the revision number of the document to delete
 * @param force  if true, forces deletion even with conflicts; if false, deletion fails on conflicts
 * @return       a Future containing the deletion response as a JsonObject with status and revision information
 * @throws IllegalArgumentException if docId or rev is null or empty
 * @throws ConcurrentModificationException if document has been modified and force is false
 */
Future<JsonObject> deleteDocument(String docId, String rev, boolean force);

  // {
  // "ok": true,
  // "id": "bfccbf7d245e15d97bb8c725a00000a4",
  // "rev": "2-eec205a9d413992850a6e32678485900"
  /**
 * Retrieves the security configuration asynchronously.
 *
 * @return a Future containing the database security configuration
 * @throws CompletionException if the security configuration cannot be retrieved
 * @see DBSecurity
 */

  Future<DBSecurity> getSecurity();

  /**
 * Sets the security configuration for the database.
 *
 * @param security The security configuration object containing access rules and permissions
 * @return A Future containing a JsonObject with the result of the security update operation
 * @throws IllegalArgumentException if the security configuration is invalid
 * @throws SecurityException if the current user lacks permissions to modify security settings
 * @see DBSecurity
 */
Future<JsonObject> setSecurity(DBSecurity security);

  /**
 * Retrieves a design document from the database asynchronously.
 *
 * @param designDocName the name of the design document to retrieve
 * @return a Future containing the requested DBDesignDoc if found
 * @throws IllegalArgumentException if designDocName is null or empty
 * @throws DatabaseException if there is an error accessing the database
 * @see DBDesignDoc
 */
Future<DBDesignDoc> getDesignDocument(String designDocName);

  /**
 * Creates a design document in the database asynchronously.
 *
 * @param designDoc the design document to be created, containing views, indexes,
 *                 and other design-time artifacts
 * @return a Future containing the server response as a JsonObject, including
 *         the document ID and revision if creation was successful
 * @throws IllegalArgumentException if designDoc is null or invalid
 * @throws DatabaseException if there's an error communicating with the database
 * @see DBDesignDoc
 */
Future<JsonObject> createDesignDocument(DBDesignDoc designDoc);

  /**
 * Updates a design document in the database with the specified revision.
 *
 * @param designDoc the design document to update containing view and index definitions
 * @param rev the revision string of the current document version
 * @return a Future containing the server response as a JsonObject with the new revision
 * @throws IllegalArgumentException if designDoc is null or rev is null/empty
 * @throws CouchDbException if there is an error communicating with the database
 * @see DBDesignDoc
 */
Future<JsonObject> updateDesignDocument(DBDesignDoc designDoc, String rev);

  /**
   * Deletes a design document from the database.
   *
   * @param designDoc the design document to delete
   * @param rev the revision string of the document
   * @return a Future containing the JSON response of the deletion operation
   * @see DBDesignDoc
   * @see JsonObject
   */
  default Future<JsonObject> deleteDesignDocument(DBDesignDoc designDoc, String rev) {
    return this.deleteDesignDocument(designDoc, rev, false);
  }

  /**
 * Deletes a design document from the database.
 *
 * @param designDocName the design document to delete
 * @param rev          the revision string of the document to delete
 * @param force        if true, bypasses any safety checks and forces deletion
 * @return             a Future containing the server response as a JsonObject
 * @throws IllegalArgumentException if designDocName is null
 * @throws CouchDbException        if there's an error communicating with the database
 * @see DBDesignDoc
 */
Future<JsonObject> deleteDesignDocument(DBDesignDoc designDocName, String rev, boolean force);
}
