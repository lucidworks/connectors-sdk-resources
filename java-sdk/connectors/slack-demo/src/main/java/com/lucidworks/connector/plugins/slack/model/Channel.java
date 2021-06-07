package com.lucidworks.connector.plugins.slack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Channel {

  private String id;
  private String name;

  public String getId() {
    return id;
  }

  public String getName() {
    return name;
  }
}