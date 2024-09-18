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

import java.util.List;

public interface QueryParameters {

  /**
   * Retrives a parameter with a given name
   *
   * @param paramName String
   * @return parameter value or null
   */
  Object getParameter(String paramName);

  /**
   * Adds a parameter
   *
   * @param paramName String
   * @param paramValue Any
   * @param force add even if paramName is not known
   */
  void addParameter(String paramName, Object paramValue, boolean force);


  /*
   * like addParameter, but only known parameters
   */
  default void addParameter(String paramName, Object paramValue) {
    addParameter(paramName, paramValue, false);
  }

  List<String> knownParameters();

  /**
   * Takes in a base url without query parameters and adds
   * known and ddefined query parameters to it
   *
   * @param sourceUrl String
   * @return complete URL
   */
  String appendParamsToUrl(final String sourceUrl);

}
