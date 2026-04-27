package com.lucidworks.connector.plugins.random.fetcher;

import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.config.RandomContentProperties;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.random.config.RandomContentConfig;
import com.lucidworks.fusion.connector.plugin.api.exceptions.ContentEmitException;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.stream.IntStream;

public class RandomContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(RandomContentFetcher.class);

  private static final String CONTENT_ID = "content-example";
  private static final String ERROR_ID = "no-number-this-should-fail";
  private static final String COUNTER_FIELD = "number";
  private static final String DOCUMENT_ID  = "document-direct-example";

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
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    if (!input.hasId()) {
      // Emit initial set of candidates
      int totalNumberOfDocs = randomContentProperties.totalNumDocs();
      IntStream.range(0, totalNumberOfDocs)
          .asLongStream()
          .forEach(i -> {
            logger.info("Emitting candidate -> number {}", i);
            fetchContext.newCandidate(String.valueOf(i))
                .metadata(m -> m.setLong(COUNTER_FIELD, i))
                .emit();
          });
      // Simulating an error item here... because we're emitting an item without a "number",
      // the fetch() call will attempt to convert the number into a long and throw an exception.
      // The item should be recorded as an error in the ConnectorJobStatus.
      fetchContext.newCandidate(ERROR_ID).emit();
      // Emits an item indicating that will produce a Content Item
      fetchContext.newCandidate(CONTENT_ID).emit();

      logger.info("Send the same document twice {}", DOCUMENT_ID);
      emitDocumentDirect(fetchContext);
      emitDocumentDirect(fetchContext);

      return fetchContext.newResult();
    }
    if (CONTENT_ID.equals(fetchContext.getFetchInput().getId())) {
      emitContent(fetchContext, input);
    } else {
      emitDocument(fetchContext, input);
    }

    return fetchContext.newResult();
  }

  private void emitDocument(FetchContext fetchContext, FetchInput input) {
    try {
      long num = (Long) input.getMetadata().get(COUNTER_FIELD);

      input.getMetadata()
          .forEach(
              (k, v) -> logger.info("Input [{}:{}[{}]]", k, v, v.getClass())
          );
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

  private void emitDocumentDirect(FetchContext fetchContext) {
    logger.info("Emitting Document without candidate -> {}", DOCUMENT_ID);

      int min = randomContentProperties.minimumNumberSentences();
      int max = randomContentProperties.maximumNumberSentences();

      String headline = generator.makeHeadline();
      String txt = generator.makeRandomText(min, max);
      fetchContext.newDocument(DOCUMENT_ID)
          .fields(f -> {
            f.setLong("timestamp", Instant.now().toEpochMilli());
            f.setString("hostname", hostname);
            f.setString("headline", headline);
            f.setString("text", txt);
            f.setLocalDateTime("crawl_date", LocalDateTime.now());
          })
          .emit();

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
    } catch(ContentEmitException e) {
      logger.error("Failed to emit content", e);
      fetchContext.newError(input.getId(), e.toString())
          .emit();
    }
  }
}
