package com.lucidworks.fusion.connector.plugin.client;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class Email {
  private String id;
  private List<String> from;
  private List<String> replyTo;
  private List<String> to;
  private List<String> cc;
  private Date date;
  private String subject;
  private String body;

  public Email() {
  }

  public Email(String from, String to, String subject, String body) {
    this.date = new Date();
    this.from = Collections.singletonList(from);
    this.to = Collections.singletonList(to);
    this.subject = subject;
    this.body = body;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public List<String> getFrom() {
    return from;
  }

  public void setFrom(List<String> from) {
    this.from = from;
  }

  public List<String> getReplyTo() {
    return replyTo;
  }

  public void setReplyTo(List<String> replyTo) {
    this.replyTo = replyTo;
  }

  public List<String> getTo() {
    return to;
  }

  public void setTo(List<String> to) {
    this.to = to;
  }

  public List<String> getCc() {
    return cc;
  }

  public void setCc(List<String> cc) {
    this.cc = cc;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getSubject() {
    return subject;
  }

  public void setSubject(String subject) {
    this.subject = subject;
  }

  public String getBody() {
    return body;
  }

  public void setBody(String body) {
    this.body = body;
  }
}
