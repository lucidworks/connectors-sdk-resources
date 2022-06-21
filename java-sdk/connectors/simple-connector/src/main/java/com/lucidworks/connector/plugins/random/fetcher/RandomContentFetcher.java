package com.lucidworks.connector.plugins.random.fetcher;

import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.config.RandomContentProperties;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.random.config.RandomContentConfig;
import com.lucidworks.fusion.connector.plugin.api.exceptions.ContentEmitException;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RandomContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(RandomContentFetcher.class);

  private static final String CONTENT_ID = "content-example";
  private static final String ERROR_ID = "no-number-this-should-fail";
  private static final String COUNTER_FIELD = "number";
  private static final String CANDIDATE_NUMBER = "candidate-number";

  private final RandomContentGenerator generator;
  private final RandomContentProperties randomContentProperties;
  private final String hostname;
  private final long numberOfCandidates;

  @Inject
  public RandomContentFetcher(
      RandomContentConfig randomContentConfig,
      RandomContentGenerator generator,
      HostnameProvider hostnameProvider
  ) {
    this.randomContentProperties = randomContentConfig.properties().getRandomContentProperties();
    this.generator = generator;
    this.hostname = hostnameProvider.get();
    this.numberOfCandidates = randomContentConfig.properties().numberOfCandidates();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    if (!input.hasId()) {
      // Emit initial set of candidates
      int totalNumberOfDocs = randomContentProperties.totalNumDocs();
      IntStream.range(0, totalNumberOfDocs)
          .asLongStream()
          .forEach(index -> emitCandidate(fetchContext, index, 0));
      // Simulating an error item here... because we're emitting an item without a "number",
      // the fetch() call will attempt to convert the number into a long and throw an exception.
      // The item should be recorded as an error in the ConnectorJobStatus.
      fetchContext.newCandidate(ERROR_ID).emit();
      // Emits an item indicating that will produce a Content Item
      fetchContext.newCandidate(CONTENT_ID).emit();
      return fetchContext.newResult();
    }

    if (CONTENT_ID.equals(fetchContext.getFetchInput().getId())) {
      emitContent(fetchContext, input);
    } else if (input.getMetadata().get(CANDIDATE_NUMBER) != null) {
      long candidate = Integer.valueOf(input.getMetadata().get(CANDIDATE_NUMBER).toString());
      if (candidate < numberOfCandidates) {
        long num = (Long) input.getMetadata().get(COUNTER_FIELD);
        emitCandidate(fetchContext, num, candidate);
        return fetchContext.newResult();
      }
      emitDocument(fetchContext, input);
    } else {
      emitDocument(fetchContext, input);
    }
    return fetchContext.newResult();
  }

  private void emitCandidate(FetchContext fetchContext, long num, long candidate) {
    String id = num + "-" + candidate;
    long incCandidate = candidate + 1;
    logger.info("Emitting candidate for item {} - number of candidate {}", num, incCandidate);
    fetchContext
        .newCandidate(id)
        .metadata(m -> {
          m.setLong(COUNTER_FIELD, num);
          m.setLong(CANDIDATE_NUMBER, incCandidate);
        })
        .emit();
  }

  private void emitDocument(FetchContext fetchContext, FetchInput input) {
    try {
      long num = (Long) input.getMetadata().get(COUNTER_FIELD);

      input.getMetadata()
          .forEach(
              (k, v) -> logger.info("Input [{}:{}[{}]]", k, v, v.getClass())
          );
      logger.info("Emitting Document -> id {} - {}", num, input.getMetadata());

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
            f.setLocalDateTime("crawl_date", LocalDateTime.now());
          })
          .emit();
    } catch (NullPointerException npe) {
      if (ERROR_ID.equals(input.getId())) {
        logger.error("The following error is expected, as means to demonstrate how errors are emitted");
        fetchContext.newError(input.getId(), "Expected exception")
            .emit();
        return;
      }
      throw npe;
    }
  }

  private void emitContent(FetchContext fetchContext, FetchInput input) {
    logger.info("Emitting content of input={}", input);
    // For Example purpose - the connector is getting a file from the resources folder and emit it as a Content.
    // In the real world, this should be a network call to some system.
    try {
      fetchContext.newContent(input.getId(), RandomContentFetcher.class.getClassLoader().getResourceAsStream("example.pdf"))
          .fields(f -> {
            f.setLong("timestamp", Instant.now().toEpochMilli());
            f.setString("hostname", hostname);
            f.setLocalDateTime("crawl_date", LocalDateTime.now());
          })
          .emit();
    } catch (ContentEmitException e) {
      logger.error("Failed to emit content", e);
      fetchContext.newError(input.getId(), e.toString())
          .emit();
    }
  }
}
