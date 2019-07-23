package com.lucidworks.fusion.connector.plugin.client;

/**
 * T is the type of folder an implementation will return.
 */
public interface ImapStore<T> {

  void connect() throws MailException;
  T getFolder(String name) throws MailException;
  void close();
  boolean isConnected();
  
}