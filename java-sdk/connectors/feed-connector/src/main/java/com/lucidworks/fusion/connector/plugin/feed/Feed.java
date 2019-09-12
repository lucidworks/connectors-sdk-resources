package com.lucidworks.fusion.connector.plugin.feed;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

public class Feed {

  private long lastUpdated;
  private Map<String, FeedEntry> entries;

  @JsonCreator
  public Feed(
      @JsonProperty("lastUpdated") long lastUpdated,
      @JsonProperty("entries") Map<String, FeedEntry> entries
  ) {
    this.lastUpdated = lastUpdated;
    this.entries = entries;
  }

  public Map<String, FeedEntry> getEntries() {
    return entries;
  }

  public long getLastUpdated() {
    return lastUpdated;
  }
}