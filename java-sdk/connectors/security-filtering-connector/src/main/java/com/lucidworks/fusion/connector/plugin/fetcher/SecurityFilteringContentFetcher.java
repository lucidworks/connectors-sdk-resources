package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.collect.Lists;
import com.lucidworks.fusion.connector.plugin.RandomContentFetcher;
import com.lucidworks.fusion.connector.plugin.RandomContentGenerator;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.api.security.AccessControlConstants.AccessControlStoreOperation;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.util.List;
import java.util.Random;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.GROUP_ID_FORMAT;

public class SecurityFilteringContentFetcher extends RandomContentFetcher {
  
  private static final Logger logger = LogManager.getLogger(SecurityFilteringContentFetcher.class);
  
  private final SecurityFilteringConfig.Properties properties;
  private final Random random;
  
  @Inject
  public SecurityFilteringContentFetcher(
      SecurityFilteringConfig config,
      RandomContentGenerator generator
  ) {
    super(config, generator);
    this.random = new Random();
    this.properties = config.properties();
  }
  
  @Override
  protected void emitDocument(
      FetchContext ctx,
      FetchInput input,
      String hostname
  ) {
    super.emitDocument(ctx, input, hostname);
    
    logger.info("Emitting document ACL for ID {}", input.getId());
    
    List<String> inbound = Lists.newArrayList(
        String.format(
            GROUP_ID_FORMAT,
            random.nextInt(properties.numberOfNestedGroups()) + 1,
            random.nextInt(properties.numberOfNestedGroups()) + 1
        )
    );
    
    ctx.emitDocumentACL(
        input.getId(),
        AccessControlStoreOperation.ADD_OR_REPLACE,
        inbound.stream().toArray(String[]::new)
    );
  }
}