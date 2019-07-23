package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.client.Email;
import com.lucidworks.fusion.connector.plugin.client.ImapClient;
import com.lucidworks.fusion.connector.plugin.client.MailException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.mail.MessagingException;
import java.util.HashMap;
import java.util.Map;

import static com.lucidworks.fusion.connector.plugin.ImapConstants.BODY_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.CC_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.DATE_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.FROM_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.ID_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.REPLY_TO_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.SUBJECT_FIELD;
import static com.lucidworks.fusion.connector.plugin.ImapConstants.TO_FIELD;



public class ImapFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(ImapFetcher.class);

  private final ImapConfig config;

  private ImapClient imapClient;

  @Inject
  public ImapFetcher(
      ImapConfig config,
      ImapClient imapClient
  ) {
    this.config = config;
    this.imapClient = imapClient;
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    try {
      for (Email message : imapClient.getUnreadMessages(this.config.properties().folder())) {
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
            data.remove(entry.getKey());
          };
        }

        fetchContext.emitDocument(data);
      }

    } catch (MailException e) {
      String message = "Failed to parse message.";
      logger.error(message, e);
      fetchContext.emitError(message);
    }

    return fetchContext.newResult();
  }

  @Override
  public StopResult stop(StopContext context) {

    try {
      this.imapClient.disconnect();
    } catch (MessagingException e) {
      logger.error("Failed to close IMAP connection.", e);
    }

    return context.newResult();
  }
}
