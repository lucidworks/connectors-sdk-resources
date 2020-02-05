package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.base.Strings;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.security.AccessControlFetcher;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SecurityFilteringAccessControlFetcher implements AccessControlFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringAccessControlFetcher.class);

  private final SecurityFilteringConfig config;

  @Inject
  public SecurityFilteringAccessControlFetcher(SecurityFilteringConfig config) {
    this.config = config;
  }

  @Override
  public PreFetchResult preFetch(PreFetchContext context) {
    //emit the default users and group
    emit(context, SecurityFilteringConstants.USER_A, SecurityFilteringConstants.USER_TYPE, null);
    emit(context, SecurityFilteringConstants.USER_B, SecurityFilteringConstants.USER_TYPE,
        SecurityFilteringConstants.GROUP_B);
    emit(context, SecurityFilteringConstants.USER_C, SecurityFilteringConstants.USER_TYPE, null);
    emit(context, SecurityFilteringConstants.GROUP_B, SecurityFilteringConstants.GROUP_TYPE, null);
    return context.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext context) {
    FetchInput input = context.getFetchInput();
    String type = (String) input.getMetadata().getOrDefault(SecurityFilteringConstants.TYPE, null);
    if (Strings.isNullOrEmpty(type)) {
      return context.newResult();
    }

    Map<String, Object> metadata = input.getMetadata();
    switch (type) {
      case SecurityFilteringConstants.PERMISSION_TYPE:
        String assigned = (String) metadata.getOrDefault(SecurityFilteringConstants.ASSIGNED, null);
        if (!Strings.isNullOrEmpty(assigned)) {
          context.newAccessControlItem(SecurityFilteringConstants.PERMISSION_TYPE)
              .addAllInbound(Arrays.asList(assigned))
              .emit();
        }
        break;

      case SecurityFilteringConstants.USER_TYPE:
        String parentGroup = (String) metadata.getOrDefault(SecurityFilteringConstants.PARENTS, null);
        List<String> outbound = Arrays.asList();
        if (!Strings.isNullOrEmpty(parentGroup)) {
          outbound = Arrays.asList(parentGroup);
        }
        context.newAccessControlItem(SecurityFilteringConstants.USER_TYPE)
            .addField("fullName", input.getId() + "FullName")
            .addField("internalId", input.getId().hashCode())
            .withUPN("someDomain@" + input.getId())
            .withSID("111-222-333-" + input.getId())
            .withSAM("someDomain\\" + input.getId())
            .withDN("A=" + input.getId() + ",B=" + input.getId())
            .addAllOutbound(outbound)
            .emit();
        break;

      case SecurityFilteringConstants.GROUP_TYPE:
        context.newAccessControlItem(SecurityFilteringConstants.GROUP_TYPE)
            .addField("fullName", input.getId() + "FullName")
            .addField("internalId", input.getId().hashCode())
            .withUPN("someDomain@" + input.getId())
            .withSID("111-222-333-" + input.getId())
            .withSAM("someDomain\\" + input.getId())
            .withDN("A=" + input.getId() + ",B=" + input.getId())
            .emit();
        break;

      default:
        logger.warn("Invalid type: [{}]", type);
    }
    return context.newResult();
  }

  private void emit(PreFetchContext context, String id, String type, String parentId) {
    Map<String, Object> metadata = new HashMap<>();
    metadata.put(SecurityFilteringConstants.TYPE, type);
    if (!Strings.isNullOrEmpty(parentId)) {
      metadata.put(SecurityFilteringConstants.PARENTS, parentId);
    }
    context.newCandidate(id).withMetadata(metadata).emit();
  }
}
