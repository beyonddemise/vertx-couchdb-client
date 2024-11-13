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
package io.vertx.ext.couchdb.parameters;

import java.util.Arrays;
import java.util.List;

import io.vertx.core.json.JsonArray;

/**
 * Query parameters when returning a document
 * see https://docs.couchdb.org/en/stable/api/document/common.html#
 */
public class DocumentGetParams extends BaseQueryParameters {

  static final List<String> keys =
      Arrays.asList("att_encoding_info", "atts_since", "conflicts", "deleted_conflicts", "latest",
          "local_seq", "meta", "open_revs", "rev", "revs", "revs_info");

  public DocumentGetParams() {
    // We retrieve attachments separately
    this.paramStore.put("attachments", false);
  }

  @Override
  public List<String> knownParameters() {
    return DocumentGetParams.keys;
  }



  /**
   * attachments (boolean) – Includes attachments bodies in response. Default is false
   * Not used here since it would break JsonObject here
   */

  /**
   * att_encoding_info (boolean) – Includes encoding information in attachment stubs if the
   * particular attachment is compressed. Default is false.
   */
  public DocumentGetParams attEncodingInfo(boolean attEncodingInfo) {
    this.paramStore.put("att_encoding_info", attEncodingInfo);
    return this;
  }

  /**
   * atts_since (array) – Includes attachments only since specified revisions. Doesn’t includes
   * attachments for specified revisions. Optional
   */
  public DocumentGetParams attsSince(JsonArray attsSince) {
    // TODO: test format
    this.paramStore.put("atts_since", urlEncoded(attsSince.encodePrettily()));
    return this;
  }

  /**
   * conflicts (boolean) – Includes information about conflicts in document. Default is false
   */
  public DocumentGetParams conflicts(boolean conflicts) {
    this.paramStore.put("conflicts", conflicts);
    return this;
  }

  /**
   * deleted_conflicts (boolean) – Includes information about deleted conflicted revisions. Default
   * is false
   */
  public DocumentGetParams deletedConflicts(boolean deletedConflicts) {
    this.paramStore.put("deleted_conflicts", deletedConflicts);
    return this;
  }

  /**
   * latest (boolean) – Forces retrieving latest “leaf” revision, no matter what rev was requested.
   * Default is false
   */
  public DocumentGetParams latest(boolean latest) {
    this.paramStore.put("latest", latest);
    return this;
  }

  /**
   * local_seq (boolean) – Includes last update sequence for the document. Default is false
   */
  public DocumentGetParams localSeq(boolean localSeq) {
    this.paramStore.put("local_seq", localSeq);
    return this;
  }

  /**
   * meta (boolean) – Acts same as specifying all conflicts, deleted_conflicts and revs_info query
   * parameters. Default is false
   */
  public DocumentGetParams meta(boolean meta) {
    this.paramStore.put("meta", meta);
    return this;
  }


  /**
   * open_revs (array) – Retrieves documents of specified leaf revisions. Additionally, it accepts
   * value as all to return all leaf revisions. Optional
   */
  public DocumentGetParams openRevs(boolean openRevs) {
    this.paramStore.put("open_revs", openRevs);
    return this;
  }

  /**
   * rev (string) – Retrieves document of specified revision. Optional
   */
  public DocumentGetParams rev(String rev) {
    this.paramStore.put("rev", rev);
    return this;
  }

  /**
   * revs (boolean) – Includes list of all known document revisions. Default is false
   */
  public DocumentGetParams revs(boolean revs) {
    this.paramStore.put("revs", revs);
    return this;
  }

  /**
   * revs_info (boolean) – Includes detailed information for all known document revisions. Default
   * is false
   */
  public DocumentGetParams revsInfo(boolean revsInfo) {
    this.paramStore.put("revs_info ", revsInfo);
    return this;
  }
}
