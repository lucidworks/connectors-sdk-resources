package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.Fetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.FetchContext;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.StartContext;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.StopContext;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.mail.*;
import java.io.IOException;
import java.util.*;


public class ImapFetcher implements Fetcher {
  private final Logger logger;
  private final ImapConfig config;

  private Store store;
  private Folder folder;

  @Inject
  public ImapFetcher(
      Logger logger,
      ImapConfig config
  ) {
    this.logger = logger;
    this.config = config;
  }

  @Override
  public StartResult start(StartContext startContext) {
    String provider;
    if(config.getProperties().getSSL()) {
      provider = "imaps";
    }
    else {
      provider = "imap";
    }

    Properties props = new Properties();
    props.setProperty("mail.store.protocol", provider);

    String host = config.getProperties().getHost();
    String username = config.getProperties().getUsername();
    String password = config.getProperties().getPassword();

    try {
      Session session = Session.getDefaultInstance(props, null);
      store = session.getStore(provider);
      store.connect(host, username, password);
      folder = store.getFolder("Inbox");
      folder.open(Folder.READ_ONLY);
    }
    catch (MessagingException e) {
      e.printStackTrace();
    }
    return StartResult.DEFAULT;
  }

  @Override
  public StopResult stop(StopContext stopContext) {
    if (folder != null && folder.isOpen()) {
      try {
        folder.close(true);
        if (store != null) {
          store.close();
        }
      } catch (MessagingException e) {
        // TODO: emit error
        e.printStackTrace();
      }
    }

    return StopResult.DEFAULT;
  }



  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    try {
      for (Message message : folder.getMessages()) {
        Map<String, Object> data = new HashMap<>();

        List<String> from = toStrings(message.getFrom());
        List<String> replyTo = toStrings(message.getReplyTo());
        List<String> to = toStrings(message.getRecipients(Message.RecipientType.TO));
        List<String> cc = toStrings(message.getRecipients(Message.RecipientType.CC));

        data.put("from", from);
        data.put("reply_to", replyTo);
        data.put("to", to);
        data.put("cc", cc);
        data.put("date", message.getReceivedDate().toInstant().toEpochMilli());
        data.put("subject", message.getSubject());
        data.put("body", getText(message));

        for(Map.Entry<String, Object> entry: data.entrySet()) {
          if(entry.getValue() == null) data.remove(entry.getKey());
        }

        fetchContext.emitDocument(data);
      }

    } catch (MessagingException e) {
      // TODO: emit error
      e.printStackTrace();
    } catch (IOException e) {
      // TODO: emit error
      e.printStackTrace();
    }

    return fetchContext.newResult();
  }

  private List<String> toStrings(Address[] addresses) {
    List<String> addrs = new ArrayList<String>();

    if(addresses != null) {
      for (Address address : addresses) {
        addrs.add(address.toString());
      }
    }

    return addrs;
  }

//  private boolean textIsHtml = false;

  /**
   * Return the primary text content of the message.
   */
  private String getText(Part p) throws
      MessagingException, IOException {
    if (p.isMimeType("text/*")) {
      String s = (String)p.getContent();
      //textIsHtml = p.isMimeType("text/html");
      return s;
    }

    if (p.isMimeType("multipart/alternative")) {
      // prefer html text over plain text
      Multipart mp = (Multipart)p.getContent();
      String text = null;
      for (int i = 0; i < mp.getCount(); i++) {
        Part bp = mp.getBodyPart(i);
        if (bp.isMimeType("text/plain")) {
          if (text == null)
            text = getText(bp);
          continue;
        } else if (bp.isMimeType("text/html")) {
          String s = getText(bp);
          if (s != null)
            return s;
        } else {
          return getText(bp);
        }
      }
      return text;
    } else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart)p.getContent();
      for (int i = 0; i < mp.getCount(); i++) {
        String s = getText(mp.getBodyPart(i));
        if (s != null)
          return s;
      }
    }

    return null;
  }
}
