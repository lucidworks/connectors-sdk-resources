package com.lucidworks.connector.plugins.security.fetcher;

import com.google.common.base.Strings;
import com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SecurityFilteringAccessControlFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringAccessControlFetcher.class);

  @Override
  public FetchResult fetch(FetchContext context) {
    FetchInput input = context.getFetchInput();
    String type = (String) input.getMetadata().getOrDefault(SecurityFilteringConstants.TYPE, null);
    logger.info("Access-control input={}, type={}", input, type);
    if (Strings.isNullOrEmpty(type)) {
      return context.newResult();
    }

    Map<String, Object> metadata = input.getMetadata();
    switch (type) {
      case SecurityFilteringConstants.PERMISSION_TYPE:
        String assigned = (String) metadata.getOrDefault(SecurityFilteringConstants.ASSIGNED, null);
        logger.info("Access-control type={}, metadata={}", type, metadata);
        if (!Strings.isNullOrEmpty(assigned)) {
          context.newAccessControl(input.getId())
              .metadata(m -> m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.PERMISSION_TYPE))
              .addAllInbound(Collections.singletonList(assigned))
              .emit();
        }
        break;

      case SecurityFilteringConstants.USER_TYPE:
        String parentGroup = (String) metadata.getOrDefault(SecurityFilteringConstants.PARENTS, null);
        logger.info("Access-control type={}, metadata={}", type, metadata);
        List<String> outbound = Collections.emptyList();
        if (!Strings.isNullOrEmpty(parentGroup)) {
          outbound = Collections.singletonList(parentGroup);
        }
        context.newAccessControl(input.getId())
            .metadata(m -> m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.USER_TYPE))
            .fields(f -> {
              f.setString("fullName", input.getId() + "FullName");
              f.setInteger("internalId", input.getId().hashCode());
            })
            .addAllOutbound(outbound)
            .emit();
        break;

      case SecurityFilteringConstants.GROUP_TYPE:
        context.newAccessControl(input.getId())
            .metadata(m -> m.setString(SecurityFilteringConstants.TYPE, SecurityFilteringConstants.GROUP_TYPE))
            .fields(f -> {
              f.setString("fullName", input.getId() + "FullName");
              f.setInteger("internalId", input.getId().hashCode());
            })
            .emit();
        break;

      default:
        logger.warn("Invalid type: [{}]", type);
    }
    return context.newResult();
  }
}
