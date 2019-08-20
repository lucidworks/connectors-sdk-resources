package com.lucidworks.fusion.connector.plugin.fetcher;

import com.lucidworks.fusion.connector.plugin.RandomContentFetcher;
import com.lucidworks.fusion.connector.plugin.RandomContentGenerator;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.GROUP_ID_FORMAT;

public class SecurityFilteringContentFetcher extends RandomContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringContentFetcher.class);

  private final SecurityFilteringConfig config;
  private final Long intervalSize;
  
  @Inject
  public SecurityFilteringContentFetcher(
      SecurityFilteringConfig config,
      RandomContentGenerator generator
  ) {
    super(config, generator);
    this.config = config;

    Long totalNumDocs = Long.valueOf(config.properties().totalNumDocs());
    Long numberOfNestedGroups = Long.valueOf(config.properties().numberOfNestedGroups());
    intervalSize =  totalNumDocs / numberOfNestedGroups;
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
      Long number = Long.valueOf(num + 1);
      Double groupLevel = Math.ceil(number.doubleValue() / intervalSize.doubleValue());

      ctx.newDocument()
          .withFields(fields)
          .withACLs(String.format(
              GROUP_ID_FORMAT,
              groupLevel.intValue(),
              rnd.nextInt(groupLevel.intValue()) + 1
          )).emit();
    } catch (NullPointerException npe) {
      if (ERROR_ID.equals(input.getId())) {
        logger.info("The following error is expected, as means to demonstrate how errors are emitted");
      }

      throw npe;
    }
  }
}
