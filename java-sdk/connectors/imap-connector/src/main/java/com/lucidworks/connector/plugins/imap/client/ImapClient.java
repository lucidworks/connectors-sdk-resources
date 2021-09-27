package com.lucidworks.connector.plugins.imap.client;


import javax.inject.Inject;
import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.search.FlagTerm;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImapClient {

  private static final Logger logger = LoggerFactory.getLogger(ImapClient.class);

  private final ImapStore<Folder> store;

  @Inject
  public ImapClient(ImapStore store) throws MailException {
    this.store = store;

    store.connect();
  }

  public List<Email> getUnreadMessages(String folderName) throws MailException {
    Folder folder = store.getFolder(folderName);

    try {
      folder.open(Folder.READ_ONLY);

      Message[] messages = folder.search(
          new FlagTerm(new Flags(Flags.Flag.SEEN), false));

      List<Email> emails = Arrays.stream(messages)
          .map(this::toEmail)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());

      folder.close(false);

      return emails;
    } catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  public void disconnect() throws MessagingException {
    if (store != null) {
      store.close();
    }
  }

  private Email toEmail(Message message) {
    try {
      List<String> from = addressToString(message.getFrom());
      List<String> replyTo = addressToString(message.getReplyTo());
      List<String> to = addressToString(message.getRecipients(Message.RecipientType.TO));
      List<String> cc = addressToString(message.getRecipients(Message.RecipientType.CC));

      // find or generate a message id
      String[] ids = message.getHeader("Message-ID");
      String id;
      if (ids != null && ids.length > 0) {
        id = ids[0];
      } else {
        id = UUID.randomUUID().toString();
      }

      return new Email(
          id,
          from,
          replyTo,
          to,
          cc,
          message.getReceivedDate(),
          message.getSubject(),
          getMessageText(message)
      );

    } catch (MessagingException | IOException e) {
      logger.error("Failed to convert message to email.", e);
      return null;
    }
  }

  private List<String> addressToString(Address[] addresses) {
    List<String> addrs = new ArrayList<>();

    if (addresses != null) {
      addrs = Arrays.stream(addresses)
          .map(a -> a.toString().replaceAll("\"", ""))
          .collect(Collectors.toList());
    }

    return addrs;
  }

  private String getMessageText(Part p) throws MessagingException, IOException {
    if (p == null) return null;
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
    } else if (p.isMimeType("multipart/*")) {
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
