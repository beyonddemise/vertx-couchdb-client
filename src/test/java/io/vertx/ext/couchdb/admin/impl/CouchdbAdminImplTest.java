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
package io.vertx.ext.couchdb.admin.impl;

import static org.junit.Assert.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class CouchdbAdminImplTest {

  @ParameterizedTest
  @MethodSource("dbNamVariations")
  void testIsValidDbName(String input, boolean result) {
    assertEquals(result, CouchdbAdminImpl.isValidDbName(input));
  }

  static Stream<Arguments> dbNamVariations() {
    return Stream.of(
        Arguments.of("sample", true),
        Arguments.of("some_db", true),
        Arguments.of("one23", true),
        Arguments.of("", false),
        Arguments.of("_demo", false),
        Arguments.of("#somthing", false),
        Arguments.of("twoo parts", false),
        Arguments.of("1db", false),
        Arguments.of("someUpper", false),
        Arguments.of("hash!#bang", false)

    );
  }

}
