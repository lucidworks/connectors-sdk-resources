package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.collect.Maps;
import com.lucidworks.fusion.connector.plugin.RandomContentFetcher;
import com.lucidworks.fusion.connector.plugin.RandomContentGenerator;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.api.security.AccessControlConstants;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.ACCESS_CONTROL;
import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.TYPE;

public class SecurityFilteringContentFetcher extends RandomContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringContentFetcher.class);
  
  @Inject
  public SecurityFilteringContentFetcher(
      SecurityFilteringConfig config,
      RandomContentGenerator generator
  ) {
    super(config, generator);
  }
  
  @Override
  protected void emitDocument(
      FetchContext ctx,
      FetchInput input,
      String hostname
  ) {
    super.emitDocument(ctx, input, hostname);
    
    logger.info("Emitting document ACL candidate for ID {}", input.getId());
    
    Map<String, Object> metadata = Maps.newHashMap();
    metadata.put(TYPE, AccessControlConstants.ACL);
    
    ctx.newCandidate(input.getId())
        .withTargetPhase(ACCESS_CONTROL)
        .withMetadata(metadata)
        .emit();
  }
}
