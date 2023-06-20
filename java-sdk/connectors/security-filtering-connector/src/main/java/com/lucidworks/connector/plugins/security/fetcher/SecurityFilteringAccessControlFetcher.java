package com.lucidworks.connector.plugins.security.fetcher;

import com.lucidworks.connector.plugins.security.SecurityConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.resource.BlobResourceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SecurityFilteringAccessControlFetcher implements ContentFetcher {
  private final static String USER1 = "user1";
  private final static String USER2 = "user2";
  private final static String GROUP1 = "group1";
  private final static String GROUP2 = "group2";
  private final static String DOC1 = "doc1";
  private final static String DOC2 = "doc2";
  private final static String ACL_FIELD = "_lw_acl_ss";

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringAccessControlFetcher.class);
  private SecurityConfig testPluginConfig;

  @Inject
  public SecurityFilteringAccessControlFetcher(SecurityConfig testPluginConfig, BlobResourceClient blobResourceClient) {
    this.testPluginConfig = testPluginConfig;
  }

  @Override
  public FetchResult fetch(FetchContext context) {
    String op = testPluginConfig.properties().crawlProperties().crudOperation();
    String id = testPluginConfig.properties().crawlProperties().acId();
    logger.info("Fetch called");
    if ("create".equalsIgnoreCase(op)) {
      createDocsAndAcs(context);
    } else if ("update".equalsIgnoreCase(op)) {
      updateACs(context, id);
    } else if ("delete".equalsIgnoreCase(op)) {
      deleteACs(context, id);
    } else {
      throw new IllegalArgumentException("Illegal operation: " + op);
    }

    return context.newResult();
  }

  private void createDocsAndAcs(FetchContext context) {
    context.newGraphAccessControl(USER1).metadata(m -> m.setString("type", "user")).addAllInbound(Collections.singletonList(USER1)).emit();
    context.newGraphAccessControl(USER2).metadata(m -> m.setString("type", "user")).addAllInbound(Collections.singletonList(USER2)).emit();
    context.newGraphAccessControl(GROUP1).metadata(m -> m.setString("type", "group")).addAllInbound(Collections.singletonList(USER1)).emit();
    List<String> users = new ArrayList();
    users.add(USER1);
    users.add(USER2);
    context.newGraphAccessControl(GROUP2).metadata(m -> m.setString("type", "group")).addAllInbound(users).emit();
    createDocuments(context);
    logger.info("Fetch created 2 users 2 groups and 2 documents");
  }

  private void createDocuments(FetchContext context) {
    context.newDocument(DOC1).fields(f -> f.setStrings(ACL_FIELD, Collections.singletonList(GROUP1))).emit();
    context.newDocument(DOC2).fields(f -> f.setStrings(ACL_FIELD, Collections.singletonList(GROUP2))).emit();
  }

  private void updateACs(FetchContext context, String id) {
    logger.info("Updating Acl with document id {} ", id);
    if (id == null) {
      throw new RuntimeException("Document id for update cannot be null");
    }
    context.newUpdateGraphAccessControlItem(id, "updateValue").emit();
  }

  private void deleteACs(FetchContext context, String id) {
    logger.info("Deleting Acl id {} ",  id);
    if (id == null) {
      throw new RuntimeException("Document id for delete cannot be null");
    }
    context.newDeleteGraphAccessControlItem(id).emit();
  }
}
