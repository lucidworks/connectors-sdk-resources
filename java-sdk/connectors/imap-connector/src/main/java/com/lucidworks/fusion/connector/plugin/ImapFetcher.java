package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.Fetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.FetchContext;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.StopContext;
import com.lucidworks.fusion.connector.plugin.client.Email;
import com.lucidworks.fusion.connector.plugin.client.ImapClient;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.*;


public class ImapFetcher implements Fetcher {
  private final Logger logger;
  private final ImapConfig config;

  private ImapClient imapClient;

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
  public FetchResult fetch(FetchContext fetchContext) {
    try {
      for (Email message : imapClient.getUnreadMessages(this.config.getProperties().getFolder())) {
        Map<String, Object> data = new HashMap<>();


        data.put(ID_FIELD, message.getId());
        data.put(FROM_FIELD, message.getFrom());
        data.put(REPLY_TO_FIELD, message.getReplyTo());
        data.put(TO_FIELD, message.getTo());
        data.put(CC_FIELD, message.getCc());
        data.put(DATE_FIELD, message.getDate().toInstant().toEpochMilli());
        data.put(SUBJECT_FIELD, message.getSubject());
        data.put(BODY_FIELD, message.getBody());

        for(Map.Entry<String, Object> entry: data.entrySet()) {
          if(entry.getValue() == null) {
            data.remove(entry.getKey())
          };
        }

        fetchContext.emitDocument(data);
      }

    } catch (MessagingException | IOException e) {
      String message = "Failed to parse message.";
      logger.error(message, e);
      fetchContext.emitError(message);
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
