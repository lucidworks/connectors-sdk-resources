package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Sets;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.PostFetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.FetchInput;
import com.lucidworks.fusion.connector.plugin.config.FeedConfig;
import com.lucidworks.fusion.connector.plugin.feed.Feed;
import com.lucidworks.fusion.connector.plugin.feed.FeedEntry;
import com.lucidworks.fusion.connector.plugin.feed.FeedGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.time.Instant;
import java.util.Map;

import static com.lucidworks.fusion.connector.plugin.api.validation.constants.FetchInputValidatorConstants.NOT_MODIFIED;

public class FeedFetcher implements ContentFetcher {

  private static final Logger logger = LoggerFactory.getLogger(FeedFetcher.class);

  private final static String CHECKPOINT_PREFIX = "checkpoint";

  private final FeedConfig connectorConfig;
  private final FeedGenerator generator;

  @Inject
  public FeedFetcher(
      FeedConfig connectorConfig,
      FeedGenerator generator
  ) {
    this.connectorConfig = connectorConfig;
    this.generator = generator;
  }

  @Override
  public FetchResult fetch(FetchContext fetchContext) {
    FetchInput input = fetchContext.getFetchInput();
    Map<String, Object> metaData = input.getMetadata();
    if (!input.hasId() || input.getId().startsWith(CHECKPOINT_PREFIX)) {
      long lastRunDateTime = 0;
      if (metaData.containsKey("lastJobRunDateTime")) {
        lastRunDateTime = (Long) metaData.get("lastJobRunDateTime");
      }
      emitCandidates(fetchContext, lastRunDateTime);
      emitCheckpoint(fetchContext, connectorConfig.properties().path(), Instant.now().toEpochMilli());
    } else {
      processFeedEntry(fetchContext, input, metaData);
    }
    return fetchContext.newResult();
  }

  private void processFeedEntry(FetchContext fetchContext, FetchInput input, Map<String, Object> metaData) {
    long lastRunDateTime = (Long) metaData.get("lastJobRunDateTime");
    long entryLastUpdated = (Long) metaData.get("lastUpdatedEntry");
    if (entryLastUpdated > lastRunDateTime) {
      fetchContext.newDocument(input.getId())
          .withFields(ImmutableMap.<String, Object>builder()
              .put("title_s", metaData.get("title"))
              .put("lastUpdatedEntry_l", entryLastUpdated)
              // adding more fields with random values.
              .putAll(generator.generateFieldsMap())
              .build()
          )
          .emit();
      logger.info("Emit document id: {}, lastUpdatedEntry {}", input.getId(), entryLastUpdated);
    } else {
      logger.info("unmodified {}", input.getId());
      fetchContext.newSkip(input.getId())
          .withConditions(Sets.newHashSet(NOT_MODIFIED))
          .emit();
    }
  }

  private void emitCandidates(FetchContext fetchContext, long lastJobRunDateTime) {
    Feed feed = generator.readFeed(connectorConfig.properties().path());
    Map<String, FeedEntry> entryMap = feed.getEntries();
    entryMap.forEach((id, entry) -> {
      fetchContext.newCandidate(entry.getId())
          .withMetadata(ImmutableMap.<String, Object>builder()
              .put("lastUpdatedEntry", entry.getLastUpdated())
              .put("title", entry.getTitle())
              .put("lastJobRunDateTime", lastJobRunDateTime)
              .build()
          )
          .withTransient(true)
          .emit();
      logger.info("Emit candidate {} to fetch", id);
    });
  }

  private void emitCheckpoint(FetchContext fetchContext, String id, long lastJobRunDateTime) {
    logger.info("Emit checkpoint with date {}", lastJobRunDateTime);
    fetchContext.newCheckpoint(String.format("%s:%s", CHECKPOINT_PREFIX, id))
        .withMetadata(ImmutableMap.<String, Object>builder()
            .put("lastJobRunDateTime", lastJobRunDateTime)
            .put("requestInfoId", fetchContext.getRequestInfo().getId())
            .build()
        )
        .emit();
  }

  @Override
  public PostFetchResult postFetch(PostFetchContext postFetchContext) {
    return postFetchContext.newResult().withPurgeStrayItems();
  }
}
