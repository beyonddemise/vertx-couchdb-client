package io.vertx.ext.couchdb.database.designdoc;

import io.vertx.core.json.JsonObject;

public class DBDesignView {

  private String mapDirective;
  private ReduceOptions reduceDirective;
  private String viewName;

  /**
   * @return the viewName
   */
  public String getViewName() {
    return viewName;
  }

  /**
   * Sets the name of the view.
   *
   * @param viewName the name to assign to this view
   */
  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  /**
   * Gets the current map representation.
   *
   * @return the string representation of the map
   */
  public String getMap() {
    return mapDirective;
  }

  /**
   * Gets the configured reduce options for this operation.
   *
   * @return the configured ReduceOptions instance, or null if no reduce options are set
   */
  public ReduceOptions getReduce() {
    return reduceDirective;
  }

  /**
   * Sets the reduce options for this operation.
   *
   * @param red the reduce options to apply
   */
  public void setReduce(ReduceOptions red) {
    this.reduceDirective = red;
  }

  /**
   * Sets the map identifier for this instance.
   *
   * @param map the string identifier of the map to be set
   */
  public void setMap(String map) {
    this.mapDirective = map;
  }

  /**
   * Creates a DBDesignView instance from a JSON object representation.
   *
   * @param jsonObj The JSON object containing view definition with "map" and "reduce" properties
   * @param viewName The name to be assigned to the view
   * @return A new DBDesignView instance populated with the JSON data
   * @throws NullPointerException if jsonObj is null
   *         The JSON object should contain:
   *         - "map": String containing the map function (required)
   *         - "reduce": String specifying the reduce function (optional)
   *         Supported reduce values:
   *         - "_count": Count reduce function
   *         - "_stats": Statistics reduce function
   *         - "_sum": Sum reduce function
   *         - "_approx_count_distinct": Approximate distinct count
   *         - "": No reduce function
   *         - Any other value: Custom reduce function
   */
  public static DBDesignView fromJson(JsonObject jsonObj, String viewName) {
    DBDesignView returnDesignView = new DBDesignView();
    String map = jsonObj.getString("map", "");
    String reduceString = jsonObj.getString("reduce", "");
    ReduceOptions finalReduce;
    switch (reduceString) {
      case "_count":
        finalReduce = ReduceOptions.COUNT;
        break;
      case "_stats":
        finalReduce = ReduceOptions.STATS;
        break;
      case "_sum":
        finalReduce = ReduceOptions.SUM;
        break;
      case "_approx_count_distinct":
        finalReduce = ReduceOptions.APPROX_COUNT_DISTINCT;
        break;
      case "":
        finalReduce = ReduceOptions.NONE;
        break;
      default:
        finalReduce = ReduceOptions.CUSTOM;
        break;
    }
    returnDesignView.setMap(map);
    returnDesignView.setViewName(viewName);
    returnDesignView.setReduce(finalReduce);
    return returnDesignView;
  }


}
