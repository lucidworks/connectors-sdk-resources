package com.lucidworks.fusion.connector.plugin.client;

import com.lucidworks.fusion.connector.plugin.ImapConfig;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import javax.mail.*;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

public class ImapClient {
  private Store store;

  @Inject
  public ImapClient(ImapConfig config, Logger logger) throws MessagingException {
    // extract properties
    String host = config.getProperties().getHost();
    String username = config.getProperties().getUsername();
    String password = config.getProperties().getPassword();
    boolean ssl = config.getProperties().getSsl();

    String provider;
    if (ssl) {
      provider = "imaps";
    } else {
      provider = "imap";
    }

    Properties props = new Properties();
    props.setProperty("mail.store.protocol", provider);

    // connect to imap server
    Session session = Session.getDefaultInstance(props, null);
    store = session.getStore(provider);
    store.connect(host, username, password);
  }

  public List<Email> getMessages(String folderName) throws MessagingException, IOException {
    validConnection();

    Folder folder = store.getFolder(folderName);
    folder.open(Folder.READ_ONLY);
    Message[] messages = folder.getMessages();
    List<Email> emails = new ArrayList<>();
    for(Message message : messages) {
      emails.add(toEmail(message));
    }

    folder.close(false);

    return emails;
  }

  public List<Email> getUnreadMessages(String folderName) throws MessagingException, IOException {
    validConnection();

    Folder folder = store.getFolder(folderName);
    folder.open(Folder.READ_ONLY);

    Message[] messages = folder.search(
        new FlagTerm(new Flags(Flags.Flag.SEEN), false));

    List<Email> emails = new ArrayList<>();
    for(Message message : messages) {
      emails.add(toEmail(message));
    }

    folder.close(false);

    return emails;
  }

  public void disconnect() throws MessagingException {
    if (store != null) {
      store.close();
    }
  }

  private Email toEmail(Message message) throws MessagingException, IOException {
    List<String> from = addressToString(message.getFrom());
    List<String> replyTo = addressToString(message.getReplyTo());
    List<String> to = addressToString(message.getRecipients(Message.RecipientType.TO));
    List<String> cc = addressToString(message.getRecipients(Message.RecipientType.CC));
    String[] ids = message.getHeader("Message-ID");

    String id;
    if(ids != null && ids.length > 0) {
      id = ids[0];
    }
    else {
      // TODO: find a more deterministic id to avoid duplicates
      id = UUID.randomUUID().toString();
    }

    Email email = new Email();
    email.setId(id);
    email.setFrom(from);
    email.setReplyTo(replyTo);
    email.setTo(to);
    email.setCc(cc);
    email.setDate(message.getReceivedDate());
    email.setSubject(message.getSubject());
    email.setBody(getMessageText(message));

    return email;
  }

  private void validConnection() throws MessagingException {
    if (this.store == null || !this.store.isConnected()) {
      throw new MessagingException("You must connect to the IMAP server before attempting to retrieve messages.");
    }
  }


  private List<String> addressToString(Address[] addresses) {
    List<String> addrs = new ArrayList<String>();

    if(addresses != null) {
      for (Address address : addresses) {
        addrs.add(address.toString().replaceAll("\"", ""));
      }
    }

    return addrs;
  }

  private String getMessageText(Part p) throws MessagingException, IOException {
    if(p == null) return null;
    if (p.isMimeType("text/*")) {
      return (String) p.getContent();
    }

    if (p.isMimeType("multipart/alternative")) {
      Multipart mp = (Multipart) p.getContent();
      String text = null;
      for (int i = 0; i < mp.getCount(); i++) {
        Part bp = mp.getBodyPart(i);
        if (bp.isMimeType("text/plain")) {
          if (text == null)
            text = getMessageText(bp);
          continue;
        } else if (bp.isMimeType("text/html")) {
          String s = getMessageText(bp);
          if (s != null)
            return s;
        } else {
          return getMessageText(bp);
        }
      }
      return text;
    }
    else if (p.isMimeType("multipart/*")) {
      Multipart mp = (Multipart) p.getContent();
      for (int i = 0; i < mp.getCount(); i++) {
        String s = getMessageText(mp.getBodyPart(i));
        if (s != null)
          return s;
      }
    }

    return null;
  }
}
