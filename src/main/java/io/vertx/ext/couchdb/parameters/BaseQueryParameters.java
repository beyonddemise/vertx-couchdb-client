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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.utils.JsonObjectSerializable;

public class BaseQueryParameters implements QueryParameters, JsonObjectSerializable {

  protected Map<String, Object> paramStore;

  @Override
  public JsonObject toJson() {
    return this.paramStore.entrySet().stream()
        .collect(Collector.of(() -> new JsonObject(),
            (j, e) -> j.put(e.getKey(), e.getValue()),
            JsonObject::mergeIn));
  }

  @Override
  public Object getParameter(String paramName) {
    return this.paramStore.get(paramName);
  }

  @Override
  public void addParameter(String paramName, Object paramValue, boolean force) {
    if (force || this.knownParameters().contains(paramName)) {
      this.paramStore.put(paramName, paramValue);
    }
  }

  @Override
  public List<String> knownParameters() {
    return new ArrayList<>();
  }

  @Override
  public String appendParamsToUrl(String sourceUrl) {
    if (this.paramStore.isEmpty()) {
      return sourceUrl;
    }

    // TODO: Can we use URiTemplate here?
    return sourceUrl + "?" + this.paramStore.entrySet().stream()
        .map(entry -> entry.getKey() + "=" + String.valueOf(entry.getValue()))
        .collect(Collectors.joining("&"));
  }

  protected String urlEncoded(String source) {
    return URLEncoder.encode(source, StandardCharsets.UTF_8);
  }

}
