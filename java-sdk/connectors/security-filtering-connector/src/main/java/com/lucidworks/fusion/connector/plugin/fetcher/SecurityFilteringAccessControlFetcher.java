package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.security.AccessControlFetcher;
import com.lucidworks.fusion.connector.plugin.api.security.AccessControlConstants;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.GROUP_ID_FORMAT;
import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.INVALID;
import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.PARENTS;
import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.TYPE;
import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.USER_ID_FORMAT;

public class SecurityFilteringAccessControlFetcher implements AccessControlFetcher {
  
  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringAccessControlFetcher.class);

  private final SecurityFilteringConfig config;
  private final Random random;
  
  @Inject
  public SecurityFilteringAccessControlFetcher(
      SecurityFilteringConfig config
  ) {
    this.config = config;
    this.random = new Random();
  }
  
  private String emitGroup(
      PreFetchContext ctx,
      int level,
      int groupCount,
      List<String> parentGroups
  ) {
    String groupId = String.format(GROUP_ID_FORMAT, level, groupCount);
    Map<String, Object> metadata = Maps.newHashMap();
    metadata.put(TYPE, AccessControlConstants.GROUP);
    metadata.put(PARENTS, parentGroups);
    ctx.newCandidate(groupId)
        .withMetadata(metadata)
        .emit();
    return groupId;
  }
  
  private List<String> processGroupLevel(
      PreFetchContext ctx,
      int level,
      List<String> parentGroups
  ) {
    List<String> levelGroups = Lists.newArrayList();
    IntStream.rangeClosed(1, level).forEach(groupCount -> {
      String groupId = emitGroup(
          ctx,
          level,
          groupCount,
          parentGroups
      );
      levelGroups.add(groupId);
    });
    return levelGroups;
  }
  
  private void emitUser(
      PreFetchContext ctx,
      int level,
      List<String> levelGroups
  ) {
    Map<String, Object> metadata = Maps.newHashMap();
    metadata.put(TYPE, AccessControlConstants.USER);
    metadata.put(PARENTS, levelGroups);
    ctx.newCandidate(String.format(USER_ID_FORMAT, level))
        .withMetadata(metadata)
        .emit();
  }
  
  @Override
  public PreFetchResult preFetch(PreFetchContext ctx) {
    if (!config.securityTrimmingIsEnabled()) {
      return ctx.newResult();
    }
    
    List<String> parentGroups = Lists.newArrayList();
    IntStream.rangeClosed(1, config.properties().numberOfNestedGroups())
        .forEach(level -> {
          List<String> levelGroups = processGroupLevel(
              ctx,
              level,
              parentGroups
          );
          
          parentGroups.clear();
          parentGroups.addAll(levelGroups);
          
          emitUser(
              ctx,
              level,
              levelGroups
          );
        });
    
    return ctx.newResult();
  }
  
  @Override
  public FetchResult fetch(FetchContext ctx) {
    FetchInput input = ctx.getFetchInput();
    
    logger.info("Processing input {}", input.getId());
    
    Map<String, Object> metadata = input.getMetadata();
    String type = (String) metadata.getOrDefault(TYPE, INVALID);
    
    if (type.equals(AccessControlConstants.ACL)) {
      ctx.newDocumentACL(input.getId())
          .withInbound(
              String.format(
                  GROUP_ID_FORMAT,
                  random.nextInt(config.properties().numberOfNestedGroups()) + 1,
                  random.nextInt(config.properties().numberOfNestedGroups()) + 1
              )
          ).emit();
    } else if (type.equals(AccessControlConstants.GROUP)) {
      ctx.newGroup(input.getId())
          .withOutbound(
              (List<String>) metadata.getOrDefault(PARENTS, Collections.emptyList())
          )
          .emit();
    } else if (type.equals(AccessControlConstants.USER)) {
      ctx.newUser(input.getId())
          .withOutbound(
              (List<String>) metadata.getOrDefault(PARENTS, Collections.emptyList())
          )
          .emit();
    } else {
      logger.error("Invalid type to be processed for input {}", input.getId());
    }
    
    return ctx.newResult();
  }
}
