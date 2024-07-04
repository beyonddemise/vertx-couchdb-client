/**
 * UnitTest annotation
 */
package io.vertx.ext.couchdb.utils;


import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import io.vertx.junit5.VertxExtension;

/**
 * Interface to mark Tests as UnitTest for JUnit
 * and Maven plugins surefire/failsave
 *
 * @author Stephan H. Wissel
 */
@Target({TYPE, METHOD, ANNOTATION_TYPE})
@Retention(RUNTIME)
@ExtendWith({VertxExtension.class, MockitoExtension.class})
@Tag("UnitTest")
public @interface UnitTest {
  // no action needed here, JUnit use only!
}
