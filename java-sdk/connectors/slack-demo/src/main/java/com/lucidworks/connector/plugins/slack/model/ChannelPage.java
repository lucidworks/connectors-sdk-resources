package com.lucidworks.connector.plugins.slack.model;

import java.util.List;
import java.util.Objects;

public class ChannelPage extends SlackResponse {

  private List<Channel> channels;

  public List<Channel> getChannels() {
    return channels;
  }

  public boolean hasNextPage() {
    return !Objects.isNull(responseMetadata) && responseMetadata.hasNextPage();
  }
}