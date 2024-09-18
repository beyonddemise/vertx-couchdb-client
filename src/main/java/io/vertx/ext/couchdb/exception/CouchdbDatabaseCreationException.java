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
package io.vertx.ext.couchdb.exception;

import io.vertx.core.json.JsonObject;

public class CouchdbDatabaseCreationException extends Exception {
  private final String error;
  private final String reason;
  private final int statusCode;
  private final JsonObject responseBody;

  public CouchdbDatabaseCreationException(String error, String reason, int statusCode,
      JsonObject responseBody) {
    super("Error creating database: " + error + " - " + reason);
    this.error = error;
    this.reason = reason;
    this.statusCode = statusCode;
    this.responseBody = responseBody;
  }

  public String error() {
    return error;
  }

  public String reason() {
    return reason;
  }

  public int statusCode() {
    return statusCode;
  }

  public JsonObject responseBody() {
    return responseBody;
  }
}
