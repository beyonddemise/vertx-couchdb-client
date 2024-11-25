package io.vertx.ext.couchdb.database.designdoc;

// public enum ReduceOptions {
//   STATS, COUNT, SUM, APPROX_COUNT_DISTINCT, CUSTOM
// }

public enum ReduceOptions {
  MAP("map"), COUNT("count"), REDUCE("reduce"), APPROX_COUNT_DISTINCT(
      "approx_count_distinct"), STATS("stats"), SUM("sum"), CUSTOM("custom");

  private final String value;

  ReduceOptions(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  // Method to map a string to the enum
  public static ReduceOptions fromString(String value) {
    for (ReduceOptions option : ReduceOptions.values()) {
      if (option.getValue().equalsIgnoreCase(value)) {
        return option;
      }
    }
    return CUSTOM;
    // throw new IllegalArgumentException("Unknown option: " + value);
  }
}
