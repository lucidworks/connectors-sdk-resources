package com.lucidworks.connector.plugins.slack;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lucidworks.connector.plugins.slack.model.ChannelPage;
import com.lucidworks.connector.plugins.slack.model.MessagePage;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.util.Objects;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SlackClient {

  private static final Logger logger = LoggerFactory.getLogger(SlackClient.class);

  private static final String SLACK_API_URL = "https://slack.com/api";
  private static final String CONVERSATIONS_LIST = "/conversations.list";
  private static final String CONVERSATIONS_HISTORY = "/conversations.history";

  private final CloseableHttpClient httpclient;

  private final SlackDemoConfig slackDemoConfig;
  private final ObjectMapper objectMapper;

  @Inject
  public SlackClient(SlackDemoConfig slackDemoConfig) {
    this.slackDemoConfig = slackDemoConfig;
    PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
    cm.setMaxTotal(20);
    httpclient = HttpClients.custom()
        .setConnectionManager(cm)
        .build();
    objectMapper = new ObjectMapper();
  }

  public ChannelPage getChannelPage(FetchInput input) throws IOException {
    String cursor = (String) input.getMetadata().get("cursor");
    URI uri = URI.create(String.format("%s%s?limit=%s&cursor=%s", SLACK_API_URL, CONVERSATIONS_LIST, slackDemoConfig.properties().pageLimit(), cursor));
    logger.info("Request URL={}", uri);
    HttpGet httpGet = new HttpGet(uri);
    httpGet.addHeader("Authorization", "Bearer " + slackDemoConfig.properties().token());

    CloseableHttpResponse response = httpclient.execute(httpGet);
    return objectMapper.readValue(response.getEntity().getContent(), ChannelPage.class);
  }

  public MessagePage getMessagePage(FetchInput input) throws IOException {
    String cursor = (String) input.getMetadata().getOrDefault("cursor", "");
    String channelId = (String) input.getMetadata().get("channelId");
    if (Objects.isNull(channelId)) {
      throw new IOException("Channel id not found in metadata");
    }

    URI uri = URI.create(String.format("%s%s?channel=%s&limit=%s&cursor=%s",
        SLACK_API_URL,
        CONVERSATIONS_HISTORY,
        channelId,
        slackDemoConfig.properties().pageLimit(),
        cursor));
    logger.info("Request URL={}", uri);
    HttpGet httpGet = new HttpGet(uri);
    httpGet.addHeader("Authorization", "Bearer " + slackDemoConfig.properties().token());

    CloseableHttpResponse response = httpclient.execute(httpGet);
    return objectMapper.readValue(response.getEntity().getContent(), MessagePage.class);
  }
}