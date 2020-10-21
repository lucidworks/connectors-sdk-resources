package com.lucidworks.connector.plugins.incremental.fetcher;

import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.incremental.config.RandomIncrementalConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncrementalContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(IncrementalContentFetcher.class);

  private final static String CHECKPOINT_PREFIX = "checkpoint_prefix";
  private final static String TOTAL_INDEXED = "total_indexed";

  private final RandomIncrementalConfig incrementalContentConfig;
  private final RandomContentGenerator generator;
  private final String hostname;

  @Inject
  public IncrementalContentFetcher(
      RandomIncrementalConfig incrementalContentConfig,
      RandomContentGenerator generator,
      HostnameProvider hostnameProvider
  ) {
    this.incrementalContentConfig = incrementalContentConfig;
    this.generator = generator;
    this.hostname = hostnameProvider.get();
  }

  @Override
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    int totalNumDocs = incrementalContentConfig.properties().getRandomContentProperties().totalNumDocs();
    if (!input.hasId()) {
      generateRandom(fetchContext, 0, totalNumDocs);
      emitCheckpoint(fetchContext, totalNumDocs);
    } else if (input.getId().equals(CHECKPOINT_PREFIX)) {
      int previousTotal = Integer.parseInt(input.getMetadata().get(TOTAL_INDEXED).toString());
      generateRandom(
          fetchContext,
          previousTotal,
          incrementalContentConfig.properties().totalNumDocsIncremental()
      );
      emitCheckpoint(
          fetchContext,
          previousTotal + incrementalContentConfig.properties().totalNumDocsIncremental()
      );
    } else {
      long num = (Long) input.getMetadata().get("number");
      Map<String, Object> fields = getFields(num);
      fetchContext.newDocument()
          .fields(f -> f.merge(fields))
          .emit();
    }
    return fetchContext.newResult();
  }

  private void emitCheckpoint(FetchContext fetchContext, int totalNumDocs) {
    logger.info("Emit checkpoint");
    fetchContext.newCheckpoint(CHECKPOINT_PREFIX)
        .metadata(m -> {
          m.setInteger(TOTAL_INDEXED, totalNumDocs);
          m.setLong("lastJobRunDateTime", Instant.now().toEpochMilli());
          m.setString("hostname", hostname);
        })
        .emit();
  }

  private void generateRandom(FetchContext fetchContext, int start, int total) {
    IntStream.range(0, total).asLongStream().forEach(index -> {
      long id = start + index;
      logger.info("Emitting candidate -> number {}", id);
      Map<String, Object> data = Collections.singletonMap("number", id);
      fetchContext.newCandidate(String.valueOf(id))
          .metadata(m -> m.merge(data))
          .withTransient(true)
          .emit();
    });
  }

  private Map<String, Object> getFields(long num) {
    int min = incrementalContentConfig.properties().getRandomContentProperties().minimumNumberSentences();
    int max = incrementalContentConfig.properties().getRandomContentProperties().maximumNumberSentences();

    String headline = generator.makeHeadline();
    String txt = generator.makeRandomText(min, max);

    Map<String, Object> fields = new HashMap<>();
    fields.put("number_i", num);
    fields.put("timestamp_l", Instant.now().toEpochMilli());
    fields.put("headline_s", headline);
    fields.put("hostname_s", hostname);
    fields.put("text_t", txt);

    return fields;
  }
}