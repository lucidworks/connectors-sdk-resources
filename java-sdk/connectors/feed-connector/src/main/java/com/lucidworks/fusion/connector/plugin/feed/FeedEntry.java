package com.lucidworks.fusion.connector.plugin.feed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class FeedEntry {

  private String id;
  private String title;
  private long lastUpdated;

  @JsonCreator
  public FeedEntry(
      @JsonProperty("id") String id,
      @JsonProperty("title") String title,
      @JsonProperty("lastUpdated") long lastUpdated
  ) {
    this.id = id;
    this.title = title;
    this.lastUpdated = lastUpdated;
  }

  public String getId() {
    return id;
  }

  public long getLastUpdated() {
    return lastUpdated;
  }

  public String getTitle() {
    return title;
  }
}
