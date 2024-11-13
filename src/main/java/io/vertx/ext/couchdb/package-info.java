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
@ModuleGen(name = "couchdb-client", groupPackage = "io.vertx.ext.couchdb")

/**
 * This package contains the main CouchDB client interface and related classes.
 * <p>
 * The {@link io.vertx.ext.couchdb.CouchdbClient} interface provides methods to
 * interact
 * with a CouchDB server, including operations for creating, reading, updating,
 * and
 * deleting documents, as well as database management functions.
 * <p>
 * Key classes in this package:
 * <ul>
 * <li>{@link io.vertx.ext.couchdb.CouchdbClient} - The main interface for
 * CouchDB operations</li>
 * <li>{@link io.vertx.ext.couchdb.admin.CouchdbAdmin} - Interface for
 * admin-level operations</li>
 * <li>{@link io.vertx.ext.couchdb.database.CouchDbDatabase} - Interface for
 * database-specific operations</li>
 * </ul>
 * <p>
 * This package is part of the Vert.x CouchDB client, which provides an
 * asynchronous API
 * for working with CouchDB in Vert.x applications.
 */
package io.vertx.ext.couchdb;

import io.vertx.codegen.annotations.ModuleGen;
