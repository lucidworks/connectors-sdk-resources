package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import java.net.InetAddress;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.IntStream;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LabConnectorFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(LabConnectorFetcher.class);
  private static final Random rnd = new Random();

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
  public PreFetchResult preFetch(PreFetchContext preFetchContext) {
    IntStream.range(0, labConnectorConfig.properties().totalNumDocs()).asLongStream().forEach(i -> {
      logger.info("Emitting candidate -> number {}", i);
      Map<String, Object> data = Collections.singletonMap("number", i);
      preFetchContext.newCandidate(String.valueOf(i))
          .withMetadata(data)
          .emit();
    });
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    String hostname = getHostname();
    emitDocument(fetchContext, input, hostname);
    return fetchContext.newResult();
  }

  protected void emitDocument(
      FetchContext fetchContext,
      FetchInput input,
      String hostname
  ) {
    long num = (Long) input.getMetadata().get("number");

    String headline = generator.makeSentence(true);
    int numSentences = getRandomNumberInRange(10, 255);
    String txt = generator.makeText(numSentences);
    logger.info("Emitting Document -> number {}", num);

    Map<String, Object> fields = new HashMap();
    fields.put("number_i", num);
    fields.put("timestamp_l", Instant.now().toEpochMilli());
    fields.put("headline_s", headline);
    fields.put("hostname_s", hostname);
    fields.put("text_t", txt);

    fetchContext.newDocument()
        .withFields(fields)
        .emit();
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