package com.lucidworks.fusion.connector.plugin.fetcher;

import com.lucidworks.connector.shared.generator.RandomContentGenerator;
import com.lucidworks.connector.shared.generator.config.RandomContentProperties;
import com.lucidworks.connector.shared.hostname.HostnameProvider;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PreFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.GROUP_ID_FORMAT;

public class SecurityFilteringContentFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(SecurityFilteringContentFetcher.class);

  private final RandomContentGenerator generator;
  private final RandomContentProperties randomContentProperties;

  private final String hostname;
  private final Long intervalSize;

  @Inject
  public SecurityFilteringContentFetcher(
      SecurityFilteringConfig config,
      RandomContentGenerator generator,
      HostnameProvider hostnameProvider
  ) {
    this.generator = generator;
    this.randomContentProperties = config.properties().getRandomContentProperties();
    Long totalNumDocs = Long.valueOf(randomContentProperties.totalNumDocs());
    Long numberOfNestedGroups = Long.valueOf(config.properties().numberOfNestedGroups());
    this.intervalSize = totalNumDocs / numberOfNestedGroups;
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
    return preFetchContext.newResult();
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    logger.info("Received FetchInput -> {}", input);
    long num = (Long) input.getMetadata().get("number");
    logger.info("Emitting Document -> number {}", num);
    emitDocument(fetchContext, num);
    return fetchContext.newResult();
  }

  private void emitDocument(FetchContext fetchContext, long num) {
    Map<String, Object> fields = getFields(num);

    double groupLevel = Math.ceil((num + 1) / intervalSize.doubleValue());

    fetchContext.newDocument()
        .withFields(fields)
        .withACLs(String.format(
            GROUP_ID_FORMAT,
            (int) groupLevel,
            1
        ))
        .emit();
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
