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
package io.vertx.ext.couchdb.database.designdoc;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import io.vertx.core.json.JsonObject;


public class DBDesignDoc {

  Map<String, DBDesignView> views = new HashMap<String, DBDesignView>(); // to use map
  private String _id;
  private String _rev;
  private String language;
  private String name;

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
  }

  /**
   * Gets the name value.
   *
   * @return the name string
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name of this entity.
   *
   * @param name the name to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Returns a map of database design views.
   *
   * @return a Map containing design view names as keys and their corresponding DBDesignView objects as values
   */
  public Map<String, DBDesignView> getViews() {
    return views;
  }


  /**
   * Removes a view from the collection of views.
   *
   * @param viewName the name of the view to be removed
   */
  public void removeView(String viewName) {
    this.views.remove(viewName);
  }

  /**
   * Adds a database design view to the collection with the specified name.
   *
   * @param viewName the unique name identifier for the view
   * @param view the database design view to be added
   * @throws NullPointerException if either viewName or view is null
   */
  public void addView(String viewName, DBDesignView view) {
    this.views.put(viewName, view);
  }

  /**
   * Sets the language for this instance.
   *
   * @param language the language code to set (e.g., "en", "es", "fr")
   * @throws IllegalArgumentException if language is null or empty
   */
  public void setLanguage(String language) {
    this.language = language;
  }

  /**
   * @return the _id
   */
  public String getId() {
    return _id;
  }

  /**
   * Gets the revision identifier of the document.
   *
   * @return the revision string that identifies the current version of the document
   */
  public String getRev() {
    return _rev;
  }

  /**
   * Converts the design document to a JSON representation compatible with CouchDB format.
   *
   * @return A JsonObject containing the design document properties including:
   *         - _id: The document ID prefixed with "_design/" if not already set
   *         - _rev: The document revision
   *         - language: The language used for views
   *         - views: An object containing all views with their map and reduce functions
   */
  public JsonObject toJson() {
    JsonObject result = new JsonObject();
    // if new Json need to figure out how to have id and rev
    if (this.getId() == null || this.getId().isEmpty()) {
      result.put("_id", "_design/" + this.getName());
    } else {
      result.put("_id", this.getId());
    }
    result.put("_rev", this.getRev());
    result.put("language", this.getLanguage());
    JsonObject viewsObject = new JsonObject();
    for (Map.Entry<String, DBDesignView> entry : this.views.entrySet()) {
      // You can directly add any object, Vert.x will handle conversion to JSON types
      viewsObject.put(entry.getKey(), new JsonObject()
          .put("map", entry.getValue().getMap())
          .put("reduce", entry.getValue().getReduce().getValue()));
    }
    result.put("views", viewsObject);
    return result;
  }

  /**
   * Converts a JsonObject representation of a design document into a DBDesignDoc object.
   *
   * @param dbSecObject The JsonObject containing the design document data. Must contain '_id' and '_rev'
   *                    fields if isNew is false.
   * @param isNew       Indicates if this is a new document. If false, '_id' and '_rev' fields are required
   *                    and validated.
   * @return A new DBDesignDoc instance populated with data from the JsonObject.
   * @throws NullPointerException if dbSecObject is null, or if '_id' or '_rev' are null when isNew is false
   */

  public static DBDesignDoc fromJson(JsonObject dbSecObject, boolean isNew) {
    if (!isNew) {
      // id and rev
      Objects.requireNonNull(dbSecObject.getString("_id"));
      Objects.requireNonNull(dbSecObject.getString("_rev"));
    }
    Objects.requireNonNull(dbSecObject);
    DBDesignDoc dbDesignDoc = new DBDesignDoc();
    dbDesignDoc._id = dbSecObject.getString("_id", "");
    dbDesignDoc._rev = dbSecObject.getString("_rev", "");
    dbDesignDoc.language = dbSecObject.getString("language", "");
    JsonObject viewsObject = dbSecObject.getJsonObject("views", new JsonObject());


    // Iterate over the keys in the outer JsonObject
    for (String key : viewsObject.fieldNames()) {
      // Get the value for the key (which should be a JsonObject)
      JsonObject innerJson = viewsObject.getJsonObject(key, new JsonObject());
      DBDesignView toView = DBDesignView.fromJson(innerJson, key);
      // Put the key and the inner JsonObject into the HashMap
      if (innerJson != null && !innerJson.isEmpty()) {
        dbDesignDoc.views.put(key, toView);
      }
    }

    return dbDesignDoc;
  }

  /**
   * Creates a DBDesignDoc instance from a JSON object representation.
   *
   * @param dbSecObject the JsonObject containing the database design document data
   * @return a new DBDesignDoc instance populated with the JSON data
   * @throws IllegalArgumentException if the JSON object is invalid or missing required fields
   */
  public static DBDesignDoc fromJson(JsonObject dbSecObject) {
    return DBDesignDoc.fromJson(dbSecObject, false);
  }
  // to implement
  // public boolean isValidJavascript (){

  // }
}
