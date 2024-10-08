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
package io.vertx.ext.couchdb;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.auth.authentication.Credentials;

/**
 * NullCredentials save us all code to check for null checking credentials
 */
public class NullCredentials implements Credentials {

  @Override
  public JsonObject toJson() {
    return new JsonObject();
  }

}
