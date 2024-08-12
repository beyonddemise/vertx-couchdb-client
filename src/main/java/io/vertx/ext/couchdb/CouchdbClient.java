package io.vertx.ext.couchdb;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
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


  /**
   * CouchDB server status as JSON object
   * <code><pre>
   * {@code
   * {
    "couchdb": "Welcome",
    "version": "3.3.3",
    "git_sha": "40afbcfc7",
    "uuid": "ef8e7f2621e53323678b03aa2e11be4b",
    "features": [
        "access-ready",
        "partitioned",
        "pluggable-storage-engines",
        "reshard",
        "scheduler"
    ],
    "vendor": {
        "name": "The Apache Software Foundation"
    }
  }
   * }
   * </pre></code>
   *
   * @return Future with the status of the CouchDB server, fails with
   *         {@link io.vertx.ext.couchdb.CouchdbException} if the operation fails
   */
  Future<JsonObject> status();

  Future<JsonArray> activeTasks();

  /**
   * List all databases
   */
  Future<JsonArray> allDbs();

  /**
   * List all databases, limited by the query parameters:
   * <ul>
   * <li>descending (boolean) – Return the databases in descending order by key. Default is
   * false.</li>
   * <li>endkey (json) – Stop returning databases when the specified key is reached.</li>
   * <li>limit (number) – Limit the number of the returned databases to the specified number.</li>
   * <li>skip (number) – Skip this number of databases before starting to return the results.
   * Default is 0.</li>
   * <li>startkey (json) – Return databases starting with the specified key.</li>
   * </ul>
   *
   * @param options JsonObject with the query parameters
   * @return Future with the list of databases, fails with
   *         {@link io.vertx.ext.couchdb.CouchdbException} if the operation fails
   */
  Future<JsonArray> allDbs(JsonObject options);

  /**
   * List all databases, limited by the query parameters:
   * <ul>
   * <li>descending (boolean) – Return the databases in descending order by key. Default is
   * false.</li>
   * <li>endkey (json) – Stop returning databases when the specified key is reached.</li>
   * <li>limit (number) – Limit the number of the returned databases to the specified number.</li>
   * <li>skip (number) – Skip this number of databases before starting to return the results.
   * Default is 0.</li>
   * <li>startkey (json) – Return databases starting with the specified key.</li>
   * </ul>
   *
   * @param options JsonObject with the query parameters
   * @return Future with the list of databases, fails with
   *         {@link io.vertx.ext.couchdb.CouchdbException} if the operation fails
   */
  Future<JsonArray> dbsInfo(JsonObject options);

  // TODO: a menthod for POST _dbs_info

  /**
   * Makes a call to the CouchDB server with the given parameters.
   * CatchALl for all other operations that don't have a specific method
   *
   * @param params JsonObject with the headers, path, method, parameters and payload (if any)
   * @return Future with the result of the call, fails with
   *         {@link io.vertx.ext.couchdb.CouchdbException} if the operation fails
   */
  Future<Buffer> rawCall(JsonObject params);

  /**
   * Creates a new database in CouchDB with the specified options.
   * This method performs a PUT request to create a database with the provided name and options.
   *
   * @param options JsonObject containing the parameters for creating the database:
   *                - "name": (String) The name of the database, must follow specific naming rules.
   *                - "q": (Integer) Optional. The number of range partitions (shards), default is 8.
   *                - "n": (Integer) Optional. The number of replicas in the cluster, default is 3.
   *                - "partitioned": (Boolean) Optional. Whether to create a partitioned database, default is false.
   * @return Future with the result of the create operation, containing the response from CouchDB.
   *         The Future fails with {@link io.vertx.ext.couchdb.CouchdbException} if the operation fails.
   *         Possible status codes:
   *         - 201 Created: Database created successfully.
   *         - 202 Accepted: Accepted, at least by one node.
   *         - 400 Bad Request: Invalid database name.
   *         - 401 Unauthorized: Administrator privileges required.
   *         - 412 Precondition Failed: Database already exists.
   */
  Future<JsonObject> createDb(JsonObject options);

}
