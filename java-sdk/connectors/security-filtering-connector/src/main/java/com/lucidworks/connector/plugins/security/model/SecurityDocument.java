package com.lucidworks.connector.plugins.security.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityDocument {

  private final Map<String, Object> fields;
  private final List<Permission> permissions;

  public SecurityDocument(Map<String, Object> fields, List<Permission> permissions) {
    this.fields = fields != null ? fields : new HashMap<>();
    this.permissions = permissions != null ? permissions : new ArrayList<>();
  }

  public Map<String, Object> getFields() {
    return fields;
  }

  public List<Permission> getPermissions() {
    return permissions;
  }
}