package com.lucidworks.fusion.connector.plugin.fetcher;

import com.lucidworks.fusion.connector.plugin.RandomContentFetcher;
import com.lucidworks.fusion.connector.plugin.RandomContentGenerator;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.GROUP_ID_FORMAT;

public class SecurityFilteringContentFetcher extends RandomContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringContentFetcher.class);

  private final SecurityFilteringConfig config;
  
  @Inject
  public SecurityFilteringContentFetcher(
      SecurityFilteringConfig config,
      RandomContentGenerator generator
  ) {
    super(config, generator);
    this.config = config;
  }
  
  @Override
  protected void emitDocument(
      FetchContext ctx,
      FetchInput input,
      long num,
      String hostname
  ) {
    try {
      Map<String, Object> fields = getFields(num, hostname);

      ctx.newDocument()
          .withFields(fields)
          .withACLs(String.format(
              GROUP_ID_FORMAT,
              rnd.nextInt(config.properties().numberOfNestedGroups()) + 1,
              rnd.nextInt(config.properties().numberOfNestedGroups()) + 1
          )).emit();
    } catch (NullPointerException npe) {
      if (ERROR_ID.equals(input.getId())) {
        logger.info("The following error is expected, as means to demonstrate how errors are emitted");
      }

      throw npe;
    }
  }
}
