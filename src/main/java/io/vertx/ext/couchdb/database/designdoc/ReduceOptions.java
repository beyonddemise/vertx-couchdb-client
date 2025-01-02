package io.vertx.ext.couchdb.database.designdoc;

// public enum ReduceOptions {
//   STATS, COUNT, SUM, APPROX_COUNT_DISTINCT, CUSTOM
// }

public enum ReduceOptions {
  COUNT("_count"), APPROX_COUNT_DISTINCT(
      "_approx_count_distinct"), STATS("_stats"), SUM("_sum"), CUSTOM("CUSTOM"), NONE("NONE");

  private final String value;

  /**
   * Constructs a ReduceOptions instance with the specified value.
   *
   * @param value The value to initialize the ReduceOptions with
   */
  ReduceOptions(String value) {
    this.value = value;
  }

  /**
   * Gets the value.
   *
   * @return the stored value
   */
  public String getValue() {
    return value;
  }

  /**
   * Maps a string value to its corresponding ReduceOptions enum constant.
   *
   * @param value the string representation to convert to enum
   * @return the matching ReduceOptions enum constant, or NONE if no match is found
   * @throws NullPointerException if the input value is null
   */
  public static ReduceOptions fromString(String value) {
    for (ReduceOptions option : ReduceOptions.values()) {
      if (option.getValue().equalsIgnoreCase(value)) {
        return option;
      }
    }
    return NONE;
  }
}
