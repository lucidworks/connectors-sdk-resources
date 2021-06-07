package com.lucidworks.connector.plugins.slack.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import java.util.Objects;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MessagePage extends SlackResponse {

  private List<Message> messages;

  public List<Message> getMessages() {
    return messages;
  }

  public boolean hasNextPage() {
    return !Objects.isNull(responseMetadata) && responseMetadata.hasNextPage();
  }
}