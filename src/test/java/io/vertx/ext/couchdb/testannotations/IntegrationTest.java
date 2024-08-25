/**
 * IntegrationTest annotation
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
