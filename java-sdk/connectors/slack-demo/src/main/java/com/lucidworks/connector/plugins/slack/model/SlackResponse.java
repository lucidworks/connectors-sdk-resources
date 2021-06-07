package com.lucidworks.connector.plugins.slack.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SlackResponse {

  private boolean ok;

  private String error;

  @JsonProperty(value = "response_metadata")
  protected ResponseMetadata responseMetadata;

  public boolean isOk() {
    return ok;
  }

  public String getError() {
    return error;
  }

  public ResponseMetadata getResponseMetadata() {
    return responseMetadata;
  }
}