package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;

public class RandomContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(RandomContentFetcher.class);

  protected final static String ERROR_ID = "no-number-this-should-fail";
  protected static final Random rnd = new Random();
  protected final RandomContentGenerator generator;

  private final RandomContentConfig randomContentConfig;

  @Inject
  public RandomContentFetcher(
      RandomContentConfig randomContentConfig,
      RandomContentGenerator generator
  ) {
    this.randomContentConfig = randomContentConfig;
    this.generator = generator;
  }

  @Override
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    IntStream.range(0, randomContentConfig.properties().totalNumDocs()).asLongStream().forEach(i -> {
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
    String hostname = getHostname();

    long num = (Long) input.getMetadata().get("number");

    logger.info("Emitting Document -> number {}", num);

    emitDocument(fetchContext, input, num, hostname);
    
    return fetchContext.newResult();
  }

  protected Map<String, Object> getFields(long num, String hostname) {
    String headline = generator.makeSentence(true);
    int numSentences = getRandomNumberInRange(10, 255);
    String txt = generator.makeText(numSentences);

    Map<String, Object> fields = new HashMap();
    fields.put("number_i", num);
    fields.put("timestamp_l", Instant.now().toEpochMilli());
    fields.put("headline_s", headline);
    fields.put("hostname_s", hostname);
    fields.put("text_t", txt);

    return fields;
  }
  
  protected void emitDocument(
      FetchContext fetchContext,
      FetchInput input,
      long num,
      String hostname
  ) {
    try {
      Map<String, Object> fields = getFields(num, hostname);
      
      fetchContext.newDocument()
          .withFields(fields)
          .emit();
    } catch (NullPointerException npe) {
      if (ERROR_ID.equals(input.getId())) {
        logger.info("The following error is expected, as means to demonstrate how errors are emitted");
      }
    
      throw npe;
    }
  }
  
  private static int getRandomNumberInRange(int min, int max) {
    if (min >= max) {
      throw new IllegalArgumentException("max must be greater than min");
    }
    return rnd.nextInt((max - min) + 1) + min;
  }

  private String getHostname() {
    try {
      return InetAddress.getLocalHost().getHostName();
    } catch (Exception ex) {
      return "no-hostname";
    }
  }
}