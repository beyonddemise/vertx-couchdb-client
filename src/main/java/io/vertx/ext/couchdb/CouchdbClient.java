package io.vertx.ext.couchdb;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.couchdb.impl.CouchdbClientImpl;

/**
 * A Vert.x service used to interact with CouchDB server instances.
 * <p>
 * Some of the operations might change <i>_id</i> field of passed {@link JsonObject} document.
 *
 * @author <a href="https://wissel.net">Stephan Wissel</a>
 */
@VertxGen
public interface CouchdbClient {

  /**
   * Create a CouchDB client which maintains its own data source.
   *
   * @param vertx the Vert.x instance
   * @param options the configuration optiond
   * @return the client
   */
  static CouchdbClient create(Vertx vertx, CouchdbClientOptions options) {
    return CouchdbClientImpl.create(vertx, options);
  }
}
