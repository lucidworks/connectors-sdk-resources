package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.collect.ImmutableMap;
import com.lucidworks.connector.plugins.fetcher.RandomContentFetcher;
import com.lucidworks.connector.plugins.client.RandomContentGenerator;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.config.RandomIncrementalConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.stream.IntStream;

public class RandomInContentIncrementalFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(RandomInContentIncrementalFetcher.class);

  private final static String CHECKPOINT_PREFIX = "checkpoint_prefix";
  private final static String TOTAL_INDEXED = "total_indexed";

  private final RandomIncrementalConfig randomContentConfig;

  @Inject
  public RandomInContentIncrementalFetcher(
      RandomIncrementalConfig randomContentConfig,
      RandomContentGenerator generator
  ) {
    //super(randomContentConfig, generator);
    this.randomContentConfig = randomContentConfig;
  }

  @Override
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    int totalNumDocs = 10;// randomContentConfig.properties().totalNumDocs();
    if (!input.hasId()) {
      generateRandom(fetchContext, 0, totalNumDocs);
      emitCheckpoint(fetchContext, totalNumDocs);
    } else if (input.getId().equals(CHECKPOINT_PREFIX)) {
      Integer previousTotal = Integer.valueOf(input.getMetadata().get(TOTAL_INDEXED).toString());
      generateRandom(
          fetchContext,
          previousTotal,
          randomContentConfig.properties().totalNumDocsIncremental()
      );
      emitCheckpoint(
          fetchContext,
          previousTotal + randomContentConfig.properties().totalNumDocsIncremental()
      );
    } else {
//      emitDocument(
//          fetchContext,
//          input,
//          (Long) input.getMetadata().get("number"),
//          getHostname()
//      );
    }
    return fetchContext.newResult();
  }

  private void emitCheckpoint(FetchContext fetchContext, int totalNumDocs) {
    logger.info("Emit checkpoint");
    fetchContext.newCheckpoint(CHECKPOINT_PREFIX)
        .withMetadata(ImmutableMap.<String, Object>builder()
            .put(TOTAL_INDEXED, totalNumDocs)
            .put("lastJobRunDateTime", Instant.now().toEpochMilli())
           // .put("hostname", getHostname())
            .build()
        )
        .emit();
  }

  private void generateRandom(FetchContext fetchContext, int start, int total) {
    IntStream.range(0, total).asLongStream().forEach(index -> {
      long id = start + index;
      logger.info("Emitting candidate -> number {}", id);
      Map<String, Object> data = Collections.singletonMap("number", id);
      fetchContext.newCandidate(String.valueOf(id))
          .withMetadata(data)
          .withTransient(true)
          .emit();
    });
  }
}