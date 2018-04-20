package com.lucidworks.fusion.connector.plugin.client.impl;

import com.lucidworks.fusion.connector.plugin.ImapConfig;
import com.lucidworks.fusion.connector.plugin.client.ImapStore;
import com.lucidworks.fusion.connector.plugin.client.MailException;

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
    this.delegate = session.getStore(provider);
  }

  @Override
  public void connect() throws MailException {
    // extract properties
    String host = config.getProperties().getHost();
    String username = config.getProperties().getUsername();
    String password = config.getProperties().getPassword();

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
