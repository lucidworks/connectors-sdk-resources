package com.lucidworks.connector.plugins.incremental.fetcher;

import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.incremental.config.RandomIncrementalConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.stream.IntStream;

public class IncrementalContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(IncrementalContentFetcher.class);

  private static final String CHECKPOINT_PREFIX = "checkpoint_prefix";
  private static final String TOTAL_INDEXED = "total_indexed";
  private static final String COUNTER_FIELD = "number";
  private static final String ERROR_ID = "no-number-this-should-fail";

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
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    int totalNumDocs = incrementalContentConfig.properties().getRandomContentProperties().totalNumDocs();
    if (!input.hasId()) {
      generateRandom(fetchContext, 0, totalNumDocs);
      // Simulating an error item here... because we're emitting an item without a "number",
      // the fetch() call will attempt to convert the number into a long and throw an exception.
      // The item should be recorded as an error in the ConnectorJobStatus.
      fetchContext.newCandidate(ERROR_ID)
          .withTransient(true)
          .emit();
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
      try {
        long num = (Long) input.getMetadata().get(COUNTER_FIELD);

        int min = incrementalContentConfig.properties().getRandomContentProperties().minimumNumberSentences();
        int max = incrementalContentConfig.properties().getRandomContentProperties().maximumNumberSentences();

        String headline = generator.makeHeadline();
        String txt = generator.makeRandomText(min, max);

        fetchContext.newDocument()
            .fields(f -> f.setLong(COUNTER_FIELD, num))
            .fields(f -> f.setLong("timestamp", Instant.now().toEpochMilli()))
            .fields(f -> f.setString("headline", headline))
            .fields(f -> f.setString("hostname", hostname))
            .fields(f -> f.setString("text_t", txt))
            .emit();
      } catch (NullPointerException npe) {
        if (ERROR_ID.equals(input.getId())) {
          // Simulating an error item here.
          logger.error("The following error is expected, as means to demonstrate how errors are emitted");
          fetchContext.newError(input.getId(), "Expected exception")
              .emit();
        }
        throw npe;
      }
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
      fetchContext.newCandidate(String.valueOf(id))
          .metadata(m -> m.setLong(COUNTER_FIELD, id))
          .withTransient(true)
          .emit();
    });
  }
}
