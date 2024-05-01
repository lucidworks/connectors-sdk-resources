package com.lucidworks.connector.plugins.security.fetcher;

import com.lucidworks.connector.plugins.security.SecurityConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.Collections;

public class SecurityFilteringAccessControlFetcher implements ContentFetcher {
  private final static String USER1 = "user1";
  private final static String USER2 = "user2";
  private final static String USER3 = "user3";
  private final static String USER4 = "user4";
  private final static String USER5 = "user5";
  private final static String GROUP1 = "group1";
  private final static String GROUP2 = "group2";
  private final static String GROUP3 = "group3";
  private final static String GROUP4 = "group4";
  private final static String DOC1 = "doc1";
  private final static String DOC2 = "doc2";
  private final static String DOC3 = "doc3";
  private final static String DOC4 = "doc4";
  private final static String DOC5 = "doc5";

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringAccessControlFetcher.class);
  private final SecurityConfig testPluginConfig;

  @Inject
  public SecurityFilteringAccessControlFetcher(SecurityConfig testPluginConfig) {
    this.testPluginConfig = testPluginConfig;
  }

  @Override
  public FetchResult fetch(FetchContext context) {
    String op = testPluginConfig.properties().crawlProperties().crudOperation();
    String id = testPluginConfig.properties().crawlProperties().acId();
    logger.info("Fetch called");
    if ("create".equalsIgnoreCase(op)) {
      createDocsAndAcs(context);
    } else if ("delete".equalsIgnoreCase(op)) {
      deleteACs(context, id);
    } else {
      throw new IllegalArgumentException("Illegal operation: " + op);
    }

    return context.newResult();
  }

  private void createDocsAndAcs(FetchContext context) {

    context.newGraphAccessControl(USER1)
        .metadata(m -> m.setString("type", "user"))
        .addToOutbound(USER1)
        .addAllInbound(Collections.singletonList(USER1))
        .emit();
    context.newGraphAccessControl(USER2)
        .metadata(m -> m.setString("type", "user"))
        .addToOutbound(USER2)
        .addAllInbound(Collections.singletonList(USER2))
        .emit();
    context.newGraphAccessControl(USER3)
        .metadata(m -> m.setString("type", "user"))
        .addToOutbound(USER3)
        .addAllInbound(Collections.singletonList(USER3))
        .emit();

    context.newGraphAccessControl(USER4)
        .metadata(m -> m.setString("type", "user"))
        .addToOutbound(USER4)
        .addAllInbound(Collections.singletonList(USER4))
        .emit();
    context.newGraphAccessControl(USER5)
        .metadata(m -> m.setString("type", "user"))
        .addToOutbound(USER5)
        .addAllInbound(Collections.singletonList(USER5))
        .emit();

    context.newGraphAccessControl(GROUP1)
        .metadata(m -> m.setString("type", "group"))
        .addToOutbound(GROUP1)
        .addAllInbound(Collections.singletonList(USER1))
        .emit();
    context.newGraphAccessControl(GROUP2)
        .metadata(m -> m.setString("type", "group"))
        .addToOutbound(GROUP2)
        .addAllInbound(Arrays.asList(USER1, USER2))
        .emit();
    context.newGraphAccessControl(GROUP3)
        .metadata(m -> m.setString("type", "group"))
        .addToOutbound(GROUP3)
        .addAllInbound(Collections.singletonList(USER3))
        .emit();

    context.newGraphAccessControl(GROUP4)
        .metadata(m -> m.setString("type", "group"))
        .addToOutbound(GROUP4)
        .addAllInbound(Arrays.asList(USER4, USER5))
        .emit();

    createDocuments(context);

    logger.info("Fetch created 3 users 3 groups and 3 documents");
  }

  private void createDocuments(FetchContext context) {
    context.newDocument(DOC1)
        .fields(f -> f.setString("description", "This is doc1"))
        .withACLs(GROUP1)
        .emit();
    context.newDocument(DOC2)
        .fields(f -> f.setString("description", "This is doc2"))
        .withACLs(GROUP2)
        .emit();
    context.newDocument(DOC3)
        .fields(f -> f.setString("description", "This is doc3"))
        .withACLs(GROUP3)
        .emit();

    // examples with deny, this will work with Fusion 5.9.4, and since Fusion 5.13.0
    context.newDocument(DOC4)
        .fields(f -> f.setString("description", "This is doc4"))
        .withACLs(GROUP4)
        .withDenyACLs(USER5)
        .emit();
    context.newDocument(DOC5)
        .fields(f -> f.setString("description", "This is doc5"))
        .withACLs(GROUP4)
        .withDenyACLs(USER4)
        .emit();
  }

  private void deleteACs(FetchContext context, String id) {
    logger.info("Deleting Acl id {} ",  id);
    if (isBlank((id))) {
      throw new RuntimeException("Document id for delete cannot be null");
    }
    context.newDeleteGraphAccessControlItem(id).emit();
  }

  private static boolean isBlank(String value) {
    return value == null || value.length() == 0;
  }
}
