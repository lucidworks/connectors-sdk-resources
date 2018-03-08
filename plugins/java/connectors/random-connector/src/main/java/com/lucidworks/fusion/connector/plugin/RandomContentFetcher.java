package com.lucidworks.fusion.connector.plugin;

import com.google.common.collect.ImmutableMap;
import com.lucidworks.fusion.connector.plugin.api.fetcher.Fetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.FetchContext;
import com.lucidworks.fusion.connector.plugin.api.fetcher.context.PreFetchContext;
import com.lucidworks.fusion.connector.plugin.api.message.fetcher.FetchInput;
import org.apache.logging.log4j.Logger;

import javax.inject.Inject;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class RandomContentFetcher implements Fetcher {

  private static final Random rnd = new Random();
  private final Logger logger;
  private final RandomContentConfig randomContentConfig;
  private final RandomContentGenerator generator;

  @Inject
  public RandomContentFetcher(
      Logger logger,
      RandomContentConfig randomContentConfig,
      RandomContentGenerator generator
  ) {
    this.logger = logger;
    this.randomContentConfig = randomContentConfig;
    this.generator = generator;
  }

  @Override
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    IntStream.range(0, randomContentConfig.getProperties().getTotalNumDocs()).asLongStream().forEach(i -> {
      logger.info("Emitting candidate -> number {}", i);
      Map<String, Object> data = Collections.singletonMap("number", i);
      preFetchContext.emitCandidate(
          String.valueOf(i), Collections.emptyMap(), data
      );
    });
    // Simulating an error item here... because we're emitting an item without a "number",
    // the fetch() call will attempt to convert the number into a long and throw an exception.
    // The item should be recorded as an error in the ConnectorJobStatus.
    preFetchContext.emitCandidate("no-number-this-should-fail");
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    String hostname = getHostname();
    long num = getNumber(input);
    String headline = generator.makeSentence(true);
    int numSentences = getRandomNumberInRange(10, 255);
    String txt = generator.makeText(numSentences);
    logger.info("Emitting Document -> number {}", num);
    fetchContext.emitDocument(ImmutableMap.<String, Object>builder()
        .put("number_i", num)
        .put("timestamp_l", Instant.now().toEpochMilli())
        .put("headline_s", headline)
        .put("hostname_s", hostname)
        .put("text_t", txt)
        .build());
    return fetchContext.newResult();
  }

  private static int getRandomNumberInRange(int min, int max) {
    if (min >= max) {
      throw new IllegalArgumentException("max must be greater than min");
    }
    return rnd.nextInt((max - min) + 1) + min;
  }

  private long getNumber(FetchInput input) {
    Object num = input.getMetadata().get("number");
    if (num instanceof Long) {
      return (Long) num;
    } else if (num instanceof Integer) {
      return (Integer) num;
    } else {
      throw new RuntimeException(String.format("Invalid value for number: %s", num));
    }
  }

  private String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (Exception ex) {
      return "no-hostname";
    }
  }
}