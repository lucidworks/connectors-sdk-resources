package com.lucidworks.connectors.plugins.imap.client.impl;

import com.lucidworks.connectors.plugins.imap.ImapConfig;
import com.lucidworks.connectors.plugins.imap.client.ImapStore;
import com.lucidworks.connectors.plugins.imap.client.MailException;

import javax.inject.Inject;
import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import java.util.Properties;

public class JavaxImapStore implements ImapStore<Folder> {

  private final Store delegate;
  private final ImapConfig config;

  @Inject
  public JavaxImapStore(ImapConfig config) throws NoSuchProviderException {
    this.config = config;

    // build session and store
    boolean ssl = config.properties().ssl();

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
    this.delegate = session.getStore(provider);
  }

  @Override
  public void connect() throws MailException {
    // extract properties
    String host = config.properties().host();
    String username = config.properties().username();
    String password = config.properties().password();

    try {
      this.delegate.connect(host, username, password);
    }
    catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  @Override
  public Folder getFolder(String name) throws MailException {
    try {
      return this.delegate.getFolder(name);
    } catch (MessagingException e) {
      throw new MailException(e);
    }
  }

  @Override
  public void close() {
    if (this.delegate != null) {
      try {
        this.delegate.close();
      } catch (MessagingException e) {
        // ignore
      }
    }
  }

  @Override
  public boolean isConnected() {
    return false;
  }
}
