package com.lucidworks.connector.plugins.fetcher;

import com.lucidworks.connector.shared.generator.RandomContentGenerator;
import com.lucidworks.connector.shared.generator.config.RandomContentProperties;
import com.lucidworks.connector.shared.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.config.RandomContentConfig;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class RandomContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(RandomContentFetcher.class);

  private static final String ERROR_ID = "no-number-this-should-fail";

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
          Map<String, Object> data = Collections.singletonMap("number", i);
          preFetchContext.newCandidate(String.valueOf(i))
              .withMetadata(data)
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
      long num = (Long) input.getMetadata().get("number");

      logger.info("Emitting Document -> number {}", num);
      Map<String, Object> fields = getFields(num);

      fetchContext.newDocument()
          .withFields(fields)
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

  private Map<String, Object> getFields(long num) {
    int min = randomContentProperties.minimumNumberSentences();
    int max = randomContentProperties.maximumNumberSentences();

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