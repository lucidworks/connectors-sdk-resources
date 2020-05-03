package com.lucidworks.connector.plugins.random.fetcher;

import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.config.RandomContentProperties;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.random.config.RandomContentConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(RandomContentFetcher.class);

  private static final String ERROR_ID = "no-number-this-should-fail";
  private static final String COUNTER_FIELD = "number";

  private final RandomContentGenerator generator;
  private final RandomContentProperties randomContentProperties;
  private final String hostname;

  @Inject
  public RandomContentFetcher(
      RandomContentConfig randomContentConfig,
      RandomContentGenerator generator,
      HostnameProvider hostnameProvider
  ) {
    this.randomContentProperties = randomContentConfig.properties().getRandomContentProperties();
    this.generator = generator;
    this.hostname = hostnameProvider.get();
  }

  @Override
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    int totalNumberOfDocs = randomContentProperties.totalNumDocs();
    IntStream.range(0, totalNumberOfDocs)
        .asLongStream()
        .forEach(i -> {
          logger.info("Emitting candidate -> number {}", i);
          Map<String, Object> data = Collections.singletonMap(COUNTER_FIELD, i);
          preFetchContext.newCandidate(String.valueOf(i))
              .metadata(m -> m.merge(data))
              .emit();
        });
    // Simulating an error item here... because we're emitting an item without a "number",
    // the fetch() call will attempt to convert the number into a long and throw an exception.
    // The item should be recorded as an error in the ConnectorJobStatus.
    preFetchContext.newCandidate(ERROR_ID)
        .emit();
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);

    emitDocument(fetchContext, input);

    return fetchContext.newResult();
  }

  private void emitDocument(FetchContext fetchContext, FetchInput input) {
    try {
      long num = (Long) input.getMetadata().get(COUNTER_FIELD);

      logger.info("Emitting Document -> number {}", num);

      int min = randomContentProperties.minimumNumberSentences();
      int max = randomContentProperties.maximumNumberSentences();

      String headline = generator.makeHeadline();
      String txt = generator.makeRandomText(min, max);

      fetchContext.newDocument()
          .fields(f -> {
            f.setLong(COUNTER_FIELD, num);
            f.setLong("timestamp", Instant.now().toEpochMilli());
            f.setString("hostname", hostname);
            f.setString("headline", headline);
            f.setString("text", txt);
            f.setDate("crawl_date", new Date());
          })
          .emit();
    } catch (NullPointerException npe) {
      if (ERROR_ID.equals(input.getId())) {
        logger.error("The following error is expected, as means to demonstrate how errors are emitted");
        fetchContext.newError(input.getId()).withError("Expected exception").emit();
        return;
      }
      throw npe;
    }
  }
}