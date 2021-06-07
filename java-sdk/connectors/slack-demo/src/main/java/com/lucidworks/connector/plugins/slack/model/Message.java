package com.lucidworks.connector.plugins.slack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Message {

  private String text;
  private String ts;

  public String getText() {
    return text;
  }

  public String getTs() {
    return ts;
  }
}