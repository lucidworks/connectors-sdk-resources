package com.lucidworks.connectors.plugins.imap.client;

public class MailException extends Exception {
  public MailException(String message) {
    super(message);
  }

  public MailException(Throwable cause) {
    super(cause);
  }
}
