package com.lucidworks.connector.plugins.security.generator;

import com.google.common.base.Strings;
import com.lucidworks.connector.plugins.security.model.Permission;
import com.lucidworks.connector.plugins.security.model.SecurityDocument;
import com.lucidworks.connector.plugins.security.util.SecurityConstants;
import com.lucidworks.connector.plugins.security.util.DocumentType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DocumentGenerator {

  public Optional<SecurityDocument> generate(String itemID, Map<String, Object> itemMetadata) {
    String type = (String) itemMetadata.getOrDefault(SecurityConstants.TYPE, null);
    if (Strings.isNullOrEmpty(type)) {
      return Optional.empty();
    }
    DocumentType documentType = DocumentType.valueOf(type);
    return generateDocument(itemID, documentType);
  }

  private Optional<SecurityDocument> generateDocument(String itemID, DocumentType documentType) {
    Map<String, Object> fields = generateFields(itemID, documentType);
    List<Permission> permissions = generatePermissions(documentType);
    return Optional.of(new SecurityDocument(fields, permissions));
  }

  private Map<String, Object> generateFields(final String itemID, final DocumentType documentType) {
    Map<String, Object> fields = new HashMap<>();
    fields.put(SecurityConstants.TYPE, documentType.name());
    fields.put("description", "description of item: " + itemID.toLowerCase());
    fields.put("title", "title - " + itemID.toLowerCase());
    return fields;
  }

  private List<Permission> generatePermissions(final DocumentType documentType) {
    List<Permission> permissions = new ArrayList<>();
    switch (documentType) {
      case DOCUMENT_TYPE_A:
        permissions.add(new Permission("permissionA", SecurityConstants.USER_A));
        break;
      case DOCUMENT_TYPE_B:
        permissions.add(new Permission("permissionB", SecurityConstants.USER_B));
        break;
      case DOCUMENT_TYPE_C:
        permissions.add(new Permission("permissionC", SecurityConstants.USER_C));
        break;
      case DOCUMENT_TYPE_D:
        permissions.add(new Permission("permissionD", SecurityConstants.GROUP_B));
        break;
    }
    return permissions;
  }
}