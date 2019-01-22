package com.lucidworks.fusion.connector.plugin.client;

import java.util.Date;
import java.util.List;

public class Email {
  private final String id;
  private final List<String> from;
  private final List<String> replyTo;
  private final List<String> to;
  private final List<String> cc;
  private final Date date;
  private final String subject;
  private final String body;

  public Email(String id, List<String> from, List<String> replyTo, List<String> to, List<String> cc, Date date, String subject, String body) {
    this.id = id;
    this.from = from;
    this.replyTo = replyTo;
    this.to = to;
    this.cc = cc;
    this.date = date;
    this.subject = subject;
    this.body = body;
  }

  public String getId() {
    return id;
  }

  public List<String> getFrom() {
    return from;
  }

  public List<String> getReplyTo() {
    return replyTo;
  }

  public List<String> getTo() {
    return to;
  }

  public List<String> getCc() {
    return cc;
  }

  public Date getDate() {
    return date;
  }

  public String getSubject() {
    return subject;
  }

  public String getBody() {
    return body;
  }
}
