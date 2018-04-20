package com.lucidworks.fusion.connector.plugin.client;

import com.google.common.collect.ImmutableMap;
import com.lucidworks.fusion.connector.plugin.ImapConfig;
import com.lucidworks.fusion.connector.plugin.ImapConfigValidator;
import com.lucidworks.fusion.schema.ModelGenerator;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
import javax.mail.search.FlagTerm;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ImapClientTest {
  private final static Logger logger = LogManager.getLogger(ImapClientTest.class);

  private String folder = "Inbox";

  private Map<String, Object> data = ImmutableMap.<String, Object>builder()
      .put("id", "imap_config")
      .put("pipelineId", "pipeline_id")
      .put("properties", ImmutableMap.<String, Object>builder()
          .put("host", "h")
          .put("username", "u")
          .put("password", "p")
          .put("ssl", true)
          .put("folder", folder)
          .build())
      .build();

  private ImapConfig config = ModelGenerator.generate(ImapConfig.class, data);

  @Test
  public void testGetMessages() throws MessagingException, MailException {
    Address from = new InternetAddress("from@nowhere.com");
    Address replyTo = new InternetAddress("reply_to@nowhere.com");
    Address to = new InternetAddress("to@nowhere.com");
    Address cc = new InternetAddress("cc@nowhere.com");
    String subject = "subject goes here";
    String body = "body goes here";

    ImapStore store = mock(ImapStore.class);

    Message myMessage = mock(Message.class);
    when(myMessage.getFrom()).thenReturn(new Address[]{from});
    when(myMessage.getReplyTo()).thenReturn(new Address[]{replyTo});
    when(myMessage.getRecipients(Message.RecipientType.TO)).thenReturn(new Address[]{to});
    when(myMessage.getRecipients(Message.RecipientType.CC)).thenReturn(new Address[]{cc});
    when(myMessage.getSubject()).thenReturn(subject);

    Folder fldr = mock(Folder.class);

    when(fldr.search(new FlagTerm(new Flags(Flags.Flag.SEEN), false))).thenReturn(new Message[]{myMessage});
    when(store.getFolder(eq(folder))).thenReturn(fldr);
    when(store.isConnected()).thenReturn(true);

    ImapClient client = new ImapClient(store, logger);

    client.getUnreadMessages(folder);
    verify(fldr, times(1)).open(eq(Folder.READ_ONLY));
    verify(fldr, times(1)).close(eq(false));

    List<Email> emails = client.getUnreadMessages(folder);
    Email email = emails.get(0);
    assertEquals(1, emails.size());
    assertEquals(from.toString(), email.getFrom().get(0));
    assertEquals(replyTo.toString(), email.getReplyTo().get(0));
    assertEquals(to.toString(), email.getTo().get(0));
    assertEquals(subject, email.getSubject());
  }
}
