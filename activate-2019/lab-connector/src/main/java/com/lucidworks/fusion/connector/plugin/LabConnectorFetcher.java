package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabConnectorFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(LabConnectorFetcher.class);

  private final LabConnectorConfig labConnectorConfig;
  private final LabConnectorGenerator generator;

  @Inject
  public LabConnectorFetcher(
      LabConnectorConfig labConnectorConfig,
      LabConnectorGenerator generator
  ) {
    logger.info("Initializing the fetcher component");
    this.labConnectorConfig = labConnectorConfig;
    this.generator = generator;
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    logger.info("fetch() was called");
    String headline = generator.makeHeadline();
    String sentence = generator.makeSentence(false);
    Map<String, Object> fields = new HashMap<>();
    fields.put("headline", headline);
    fields.put("sentence", sentence);
    logger.info("Emitting test document");
    fetchContext.newDocument("test").withFields(fields).emit();
    return fetchContext.newResult();
  }

}