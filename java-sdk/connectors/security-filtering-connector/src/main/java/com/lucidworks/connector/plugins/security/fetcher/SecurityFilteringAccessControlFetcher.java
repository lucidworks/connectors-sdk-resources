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

/* - Run the connector with CRUD Operation set to "create" and acId set to N.A. (or any other values)
   - In the Query Work Bench, add a Graph Security Trimming stage with:
       - "ACL Solr Collection" set to the same as the
       - Keep "User ID Source" and "User ID Key" as the default
       - Set "Join Field" to _lw_acl_ss
       - Set Join Method to topLevelDV
   - Edit Query Work Bench parameters and add {username, user1}
   - run the query id:doc* - you should get doc1 and doc2 because user1 is in group1 and group2
   - Edit Query Work Bench parameters and change {username, user2}
   - run the query id:doc* - you should get only doc2 because user2 is only in group2
   - Remove the Graph Security Trimming stage and run the connector with CRUD Operation set to "delete" and acId set to "group1" and see group1 deleted
   - Run the connector with CRUD Operation set to "update" and acId set to "user1___0" and see user1 deleted
 */
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
    context.newGraphAccessControl(GROUP1).metadata(m -> m.setString("type", "group")).addAllInbound(Collections.singletonList(USER1)).addAllOutbound(Collections.singletonList(GROUP1)).emit();
    List<String> users = new ArrayList();
    users.add(USER1);
    users.add(USER2);
    context.newGraphAccessControl(GROUP2).metadata(m -> m.setString("type", "group")).addAllInbound(users).addAllOutbound(Collections.singletonList(GROUP2)).emit();
    createDocuments(context);
    logger.info("Fetch created 2 users 2 groups and 2 documents");
  }

  private void createDocuments(FetchContext context) {
    context.newDocument(DOC1).fields(f -> f.setStrings(ACL_FIELD, Collections.singletonList(GROUP1))).emit();
    context.newDocument(DOC2).fields(f -> f.setStrings(ACL_FIELD, Collections.singletonList(GROUP2))).emit();
  }

  private void updateACs(FetchContext context, String id) {
    logger.info("Updating {} id {} ", USER1, id);
    context.newUpdateGraphAccessControlItem(id, "updateValue").emit();
  }

  private void deleteACs(FetchContext context, String id) {
    logger.info("Deleting {}  id {} ", GROUP1, id);
    context.newDeleteGraphAccessControlItem(id).emit();
  }
}
