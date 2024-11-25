package io.vertx.ext.couchdb.database.designdoc;

import io.vertx.core.json.JsonObject;

public class DBDesignView {

  private String map;
  private ReduceOptions reduce;
  private String viewName;

  /**
   * @return the viewName
   */
  public String getViewName() {
    return viewName;
  }

  /**
   * @param viewName the viewName to set
   */
  public void setViewName(String viewName) {
    this.viewName = viewName;
  }

  /**
   * @return the map
   */
  public String getMap() {
    return map;
  }

  /**
   * @return the reduce
   */
  public ReduceOptions getReduce() {
    return reduce;
  }

  /**
   * @return the reduce to set
   */
  public ReduceOptions setReduce(ReduceOptions reduce) {
    return reduce;
  }

  /**
   * @param map the map to set
   */
  public void setMap(String map) {
    this.map = map;
  }

  public static DBDesignView fromJson(JsonObject jsonObj) {
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
      default:
        finalReduce = ReduceOptions.CUSTOM;
        break;
    }
    returnDesignView.setMap(map);
    returnDesignView.setReduce(finalReduce);
    return returnDesignView;
  }


}
