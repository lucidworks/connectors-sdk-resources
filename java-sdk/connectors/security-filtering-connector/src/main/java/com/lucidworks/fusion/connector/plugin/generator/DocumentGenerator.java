package com.lucidworks.fusion.connector.plugin.generator;

import com.google.common.base.Strings;
import com.lucidworks.fusion.connector.plugin.model.Permission;
import com.lucidworks.fusion.connector.plugin.model.SecurityDocument;
import com.lucidworks.fusion.connector.plugin.util.DocumentType;
import com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class DocumentGenerator {

  public Optional<SecurityDocument> generate(String itemID, Map<String, Object> itemMetadata) {
    String type = (String) itemMetadata.getOrDefault(SecurityFilteringConstants.TYPE, null);
    if (Strings.isNullOrEmpty(type)) {
      return Optional.empty();
    }
    DocumentType documentType = DocumentType.valueOf(type);
    switch (documentType) {
      case DOCUMENT_TYPE_A:
        return generateDocument(itemID, DocumentType.DOCUMENT_TYPE_A);
      case DOCUMENT_TYPE_B:
        return generateDocument(itemID, DocumentType.DOCUMENT_TYPE_B);
      case DOCUMENT_TYPE_C:
        return generateDocument(itemID, DocumentType.DOCUMENT_TYPE_C);
      case DOCUMENT_TYPE_D:
        return generateDocument(itemID, DocumentType.DOCUMENT_TYPE_D);
      default:
        return Optional.empty();
    }
  }

  private Optional<SecurityDocument> generateDocument(String itemID, DocumentType documentType) {
    Map<String, Object> fields = new HashMap<>();
    fields.put(SecurityFilteringConstants.TYPE, documentType.name());
    fields.put("description", "description of item: " + itemID.toLowerCase());
    fields.put("title", "title - " + itemID.toLowerCase());
    List<Permission> permissions = new ArrayList<>();
    permissions.add(new Permission("userPermission", SecurityFilteringConstants.USER_A));
    permissions.add(new Permission("groupPermission", SecurityFilteringConstants.GROUP_B));
    return Optional.of(new SecurityDocument(fields, permissions));
  }
}