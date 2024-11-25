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

import java.util.List;
import java.util.Objects;
import java.util.Set;

import io.vertx.core.json.JsonObject;


public class DBDesignDoc {

  private List<DBDesignView> views;
  private String _id;
  private String _rev;
  private String language;

  /**
   * @return the language
   */
  public String getLanguage() {
    return language;
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
    result.put("_id", this.getId());
    result.put("_rev", this.getRev());
    result.put("language", this.getLanguage());
    JsonObject viewsObject = new JsonObject();
    this.views.forEach(view -> {
      viewsObject.put(view.getViewName(),
          new JsonObject()
              .put("map", view.getMap())
              .put("reduce", view.getReduce()));
    });
    result.put("views", viewsObject);
    return result;
  }

  // {
  // "_id": "_design/newDesignDoc",
  // "_rev": "4-2978513866fe9cf18602bea96680fe6b",
  // "views": {
  // "sample-view": {
  // "map": "function (doc) {\n emit(doc._id, 1);\n}"
  // },
  // "new-view2": {
  // "map": "function (doc) {\n emit(doc._id, 1);\n}",
  // "reduce": "_count"
  // },
  // "new-view3": {
  // "reduce": "function (keys, values, rereduce) {\n if (rereduce) {\n return sum(values);\n } else
  // {\n return values.length;\n }\n}",
  // "map": "function (doc) {\n emit(doc._id, 1);\n}"
  // }
  // },
  // "language": "javascript"
  // }
  public static DBDesignDoc fromJson(JsonObject dbSecObject) {
    Objects.requireNonNull(dbSecObject);
    DBDesignDoc dbDesignDoc = new DBDesignDoc();
    dbDesignDoc._id = dbSecObject.getString("_id", "");
    dbDesignDoc._rev = dbSecObject.getString("_rev", "");
    dbDesignDoc.language = dbSecObject.getString("language", "");
    JsonObject viewsObject = dbSecObject.getJsonObject("views", new JsonObject());
    // get jsonobject names
    Set<String> fieldNames = viewsObject.fieldNames();

    fieldNames.forEach(viewName -> {
      DBDesignView designView =
          DBDesignView.fromJson(viewsObject.getJsonObject(viewName, new JsonObject()));
      dbDesignDoc.views.add(designView);
    });

    return dbDesignDoc;
  }

  // to implement
  // public boolean isValidJson(){

  // }
}
