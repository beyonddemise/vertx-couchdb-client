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
package io.vertx.ext.couchdb.streams;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.core.streams.WriteStream;

@VertxGen
public interface CouchDbStream extends ReadStream<JsonObject> {

  CouchDbStream fetch(long batchSize);

  CouchDbStream resume();

  CouchDbStream pause();

  CouchDbStream endHandler(Handler<Void> endHandler);

  CouchDbStream handler(Handler<JsonObject> handler);

  CouchDbStream exceptionHandler(Handler<Throwable> exceptionHandler);

  default Future<Void> pipeTo(WriteStream<JsonObject> destination) {
    return ReadStream.super.pipeTo(destination);
  }
}
