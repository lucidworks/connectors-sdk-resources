package com.lucidworks.connector.plugins.security.model;

public class Permission {

  private final String id;
  private final String assigned;

  public Permission(String id, String assigned) {
    this.id = id;
    this.assigned = assigned;
  }

  public String getId() {
    return id;
  }

  public String getAssigned() {
    return assigned;
  }
}
