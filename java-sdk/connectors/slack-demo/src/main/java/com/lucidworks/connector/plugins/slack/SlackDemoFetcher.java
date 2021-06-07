package com.lucidworks.connector.plugins.slack;

import com.lucidworks.connector.plugins.slack.model.Channel;
import com.lucidworks.connector.plugins.slack.model.ChannelPage;
import com.lucidworks.connector.plugins.slack.model.Message;
import com.lucidworks.connector.plugins.slack.model.MessagePage;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.io.IOException;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackDemoFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SlackDemoFetcher.class);
  private final SlackClient client;

  @Inject
  public SlackDemoFetcher(SlackClient client) {
    logger.info("starting");
    this.client = client;
  }

  @Override
  public StartResult start(StartContext context) {
    return ContentFetcher.super.start(context);
  }

  @Override
  public FetchResult fetch(FetchContext context) {
    FetchInput input = context.getFetchInput();
    logger.info("fetching={}", input);

    try {
      Type type = getType(input);
      switch (type) {
        case INITIAL:
          emitChannelsPageCandidate(context, "");
          break;
        case CHANNEL_PAGE:
          handleChannelPage(context, input);
          break;
        case MESSAGES_PAGE:
          handleMessagesPage(context, input);
          break;
        case UNKNOWN:
          break;
      }
    } catch (IOException e) {
      logger.error("error while processing input={}", input, e);
      context.newError(input.getId(), e.getMessage());
    }
    return context.newResult();
  }

  private void emitChannelsPageCandidate(FetchContext context, String cursor) {
    context.newCandidate(String.format("channel_page_%s", random()))
        .metadata(map -> {
          map.setString("cursor", cursor);
          map.setString("type", Type.CHANNEL_PAGE.toString());
        })
        .emit();
  }

  //emit document per channel
  //emit candidate per channel, for messages
  private void handleChannelPage(FetchContext context, FetchInput input) throws IOException {
    ChannelPage channelPage = client.getChannelPage(input);
    if (!channelPage.isOk()) {
      throw new IOException("Invalid response=" + channelPage.getError());
    }

    channelPage.getChannels().forEach(channel -> {
      emitChannelDocument(context, channel);
      emitMessagePageCandidate(context, channel.getId(), "");//first page
    });

    if (channelPage.hasNextPage()) {
      emitChannelsPageCandidate(context, channelPage.getResponseMetadata().getNextCursor());
    }
  }

  private void emitChannelDocument(FetchContext context, Channel channel) {
    context.newDocument(channel.getId())
        .fields(map -> {
          map.setString("name", channel.getName());
          map.setString("type", "channel");
        })
        .emit();
  }

  private void emitMessageDocument(FetchContext context, Message message) {
    context.newDocument(message.getTs())
        .fields(map -> {
          map.setString("text", message.getText());
          map.setString("type", "message");
        })
        .emit();
  }

  private void emitMessagePageCandidate(FetchContext context, String channelId, String cursor) {
    context.newCandidate(String.format("message-page_%s_%s", channelId, random()))
        .metadata(map -> {
          map.setString("type", Type.MESSAGES_PAGE.toString());
          map.setString("channelId", channelId);
          map.setString("cursor", cursor);
        })
        .emit();
  }

  //emit document per message
  //emit candidate for next pages
  private void handleMessagesPage(FetchContext context, FetchInput input) throws IOException {
    MessagePage messagePage = client.getMessagePage(input);
    if (!messagePage.isOk()) {
      throw new IOException("Invalid response=" + messagePage.getError());
    }

    messagePage.getMessages().forEach(message -> emitMessageDocument(context, message));

    if (messagePage.hasNextPage()) {
      emitMessagePageCandidate(context, (String) input.getMetadata().get("channelId"), messagePage.getResponseMetadata().getNextCursor());
    }
  }

  private Type getType(FetchInput input) {
    if (!input.hasId()) {
      return Type.INITIAL;
    } else {
      String type = (String) input.getMetadata().getOrDefault("type", "");
      try {
        return Type.valueOf(type);
      } catch (IllegalArgumentException e) {
        return Type.UNKNOWN;
      }
    }
  }

  @Override
  public StopResult stop(StopContext context) {
    logger.info("stopping");
    return ContentFetcher.super.stop(context);
  }

  private static String random() {
    return RandomStringUtils.randomAlphabetic(20);
  }
}