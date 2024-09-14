
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
package io.vertx.ext.couchdb.testannotations;


import io.vertx.junit5.VertxExtension;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.testcontainers.junit.jupiter.TestcontainersExtension;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Interface to mark Tests as IntegrationTests for JUnit
 * and Maven plugins surefire/failsave
 *
 * @author Jansen Ang
 */
@Target({TYPE, METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@ExtendWith({VertxExtension.class, TestcontainersExtension.class})
@Tag("IntegrationTest")
public @interface IntegrationTest {
  // no action needed here, JUnit use only!
}
