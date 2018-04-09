package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.Fetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.FetchContext;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.StartContext;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.StopContext;
import com.lucidworks.fusion.connector.plugin.client.Email;
import com.lucidworks.fusion.connector.plugin.client.ImapClient;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class ImapFetcher implements Fetcher {
  private final Logger logger;
  private final ImapConfig config;

  private ImapClient imapClient;
  private String folderName;

  @Inject
  public ImapFetcher(
      Logger logger,
      ImapConfig config,
      ImapClient imapClient
  ) {
    this.logger = logger;
    this.config = config;
    this.imapClient = imapClient;
  }

  @Override
  public StartResult start(StartContext startContext) {
    String host = config.getProperties().getHost();
    String username = config.getProperties().getUsername();
    String password = config.getProperties().getPassword();
    boolean ssl = config.getProperties().getSsl();
    this.folderName = config.getProperties().getFolder();

    try {
      this.imapClient.connect(host, username, password, ssl);
    }
    catch (MessagingException e) {
      logger.error("Failed to open IMAP connection.", e);
    }
    return StartResult.DEFAULT;
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    try {
      for (Email message : imapClient.getUnreadMessages(this.folderName)) {
        Map<String, Object> data = new HashMap<>();


        data.put("id", message.getId());
        data.put("from", message.getFrom());
        data.put("reply_to", message.getReplyTo());
        data.put("to", message.getTo());
        data.put("cc", message.getCc());
        data.put("date", message.getDate().toInstant().toEpochMilli());
        data.put("subject", message.getSubject());
        data.put("body", message.getBody());

        for(Map.Entry<String, Object> entry: data.entrySet()) {
          if(entry.getValue() == null) data.remove(entry.getKey());
        }

        fetchContext.emitDocument(data);
      }

    } catch (MessagingException | IOException e) {
      logger.error("Failed to parse message.", e);
      fetchContext.emitError("Failed to parse message");
    }

    return fetchContext.newResult();
  }

  @Override
  public StopResult stop(StopContext stopContext) {
    try {
      this.imapClient.disconnect();
    }
    catch (MessagingException e) {
      logger.error("Failed to close IMAP connection.", e);
    }

    return StopResult.DEFAULT;
  }
}
