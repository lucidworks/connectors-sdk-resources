package com.lucidworks.connector.plugins.slack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ResponseMetadata {

  @JsonProperty(value = "next_cursor")
  private String nextCursor;

  public String getNextCursor() {
    return nextCursor;
  }

  public boolean hasNextPage() {
    return !Strings.isNullOrEmpty(nextCursor);
  }
}