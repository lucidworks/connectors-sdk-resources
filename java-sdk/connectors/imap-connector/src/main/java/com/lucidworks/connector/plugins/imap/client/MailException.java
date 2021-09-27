package com.lucidworks.connector.plugins.imap.client;

public class MailException extends Exception {
  public MailException(String message) {
    super(message);
  }

  public MailException(Throwable cause) {
    super(cause);
  }
}
