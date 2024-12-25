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

  public String getName() {
    return name;
  }

  /**
   * @param language the language to set
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * @return the language
   */
  public Map<String, DBDesignView> getViews() {
    return views;
  }


  public void removeView(String viewName) {
    this.views.remove(viewName);
  }

  public void addView(String viewName, DBDesignView view) {
    this.views.put(viewName, view);
  }

  /**
   * @param language the language to set
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
   * @return the _rev
   */
  public String getRev() {
    return _rev;
  }

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

  // updateServerResponseMerge function check ID and update the rev

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

  public static DBDesignDoc fromJson(JsonObject dbSecObject) {
    return DBDesignDoc.fromJson(dbSecObject, false);
  }
  // to implement
  // public boolean isValidJavascript (){

  // }
}
