package examples;

public class CouchDbClientExample {

  public void create(Vertx vertx) {
    WebClient webClient = WebClient.create(vertx);
    CouchdbClientBuilder builder = new CouchdbClientBuilder(vertx, webClient);
    CouchdbClient couchDbClient = builder.build();
  }

  public void createAuthenticated(Vertx vertx, Credentials credentials) {
    WebClient webClient = WebClient.create(vertx);
    CouchdbClientBuilder builder = new CouchdbClientBuilder(vertx, webClient);
    builder.credentials(credentials);
    CouchdbClient couchDbClient = builder.build();
  }

  public void createFull(Vertx vertx, Credentials credentials, int port, String host, boolean isTls) {
    WebClient webClient = WebClient.create(vertx);
    CouchdbClient couchDbClient = new CouchdbClientBuilder(vertx, webClient)
      .credentials(credentials)
      .host(host)
      .port(port)
      .https(isTls)
      .build();
  }

  
  // TODO: implement more
}
