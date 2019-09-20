package com.lucidworks.fusion.connector.plugin.fetcher;

import com.google.common.base.Strings;
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
  private final static String LAST_JOB_RUN_DATE_TIME = "lastJobRunDateTime";
  private final static String ENTRY_LAST_UPDATED = "lastUpdatedEntry";
  private final static String ENTRY_INDEX_START = "entryIndexStart";
  private final static String ENTRY_INDEX_END = "entryIndexEnd";

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
      long currentJobRunDateTime = Instant.now().toEpochMilli();
      long lastJobRunDateTime = 0;
      if (metaData.containsKey(LAST_JOB_RUN_DATE_TIME)) {
        // extract the lastJobRunDateTime from the checkpoint coming from crawlDb
        // it represents the last time a job was run (previous to this current crawl)
        lastJobRunDateTime = (Long) metaData.get(LAST_JOB_RUN_DATE_TIME);
      }
      Feed feed = getFeedFile(getEntryIndexStart(input), getEntryIndexEnd(input));
      emitCandidates(feed, fetchContext, lastJobRunDateTime);
      // add/update the checkpoint
      emitCheckpoint(
          fetchContext,
          currentJobRunDateTime,
          getEntryIndexStart(input),
          getEntryIndexEnd(input)
      );
    } else {
      processFeedEntry(fetchContext, input, metaData);
    }
    return fetchContext.newResult();
  }

  @Override
  public PostFetchResult postFetch(PostFetchContext postFetchContext) {
    // Enabling 'purge stray items' allows deletion of stray content.
    // An item is considered ‘stray’ if its last modified date in crawlDB (modifiedAt_l) is less than startJobTime of the
    // current crawl, it means, items which were not emitted during the current crawl.
    // Deletion will happen in postFetch time in the server side.
    return postFetchContext.newResult().withPurgeStrayItems();
  }

  private void processFeedEntry(FetchContext fetchContext, FetchInput input, Map<String, Object> metaData) {
    long lastJobRunDateTime = (Long) metaData.get(LAST_JOB_RUN_DATE_TIME);
    long entryLastUpdated = (Long) metaData.get(ENTRY_LAST_UPDATED);
    // when candidate was emitted, it was provided 'lastJobRunDateTime'. it will be compared with 'lastUpdatedEntry'
    // If true it means an entry was updated after the last time a job was run, the emit the item as document in order to be re-indexed.
    if (entryLastUpdated > lastJobRunDateTime) {
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
      // if false, it means the entry was not modified.
      // We need to emit it as unmodified to update the 'modifiedAt_l' in crawlDb item, it is mandatory otherwise the
      // purge process will remove unmodified items.
      logger.info("unmodified {}", input.getId());
      fetchContext.newSkip(input.getId())
          .withConditions(Sets.newHashSet(NOT_MODIFIED))
          .emit();
    }
  }

  private void emitCandidates(Feed feed, FetchContext fetchContext, long lastJobRunDateTime) {
    Map<String, FeedEntry> entryMap = feed.getEntries();
    entryMap.forEach((id, entry) -> {
      fetchContext.newCandidate(entry.getId())
          .withMetadata(ImmutableMap.<String, Object>builder()
              .put("title", entry.getTitle())
              // add last time when entry was modified
              .put(ENTRY_LAST_UPDATED, entry.getLastUpdated())
              // add 'lastJobRunDateTime'.
              .put(LAST_JOB_RUN_DATE_TIME, lastJobRunDateTime)
              .build()
          )
          // for feed-connector purposes, we do not want to reevaluate the crawlDb items in subsequent crawls (except checkpoints)
          // so, we will emit candidates as transient=true
          .withTransient(true)
          .emit();
      logger.info("Emit candidate {} to fetch", id);
    });
  }

  private void emitCheckpoint(
      FetchContext fetchContext,
      long currentJobRunDateTime,
      int entryIndexStart,
      int entryIndexEnd
  ) {
    // For feed-connector purposes, we need to update the checkpoint per crawl
    // Reason is to update the lastJobRunDateTime with the current crawl time.
    logger.info("Emit checkpoint with date {}", currentJobRunDateTime);
    fetchContext.newCheckpoint(CHECKPOINT_PREFIX)
        .withMetadata(ImmutableMap.<String, Object>builder()
            .put(LAST_JOB_RUN_DATE_TIME, currentJobRunDateTime)
            .put("requestInfoId", fetchContext.getRequestInfo().getId())
            .put(ENTRY_INDEX_START,
                entryIndexStart) // needed when generating entries (entries are not get from a json file)
            .put(ENTRY_INDEX_END,
                entryIndexEnd) // needed when generating entries (  entries are not get from a json file)
            .build()
        )
        .emit();
  }

  private Feed getFeedFile(int entryIndexStart, int entryIndexEnd) {
    if (!Strings.isNullOrEmpty(connectorConfig.properties().path())) {
      return generator.readFeed(connectorConfig.properties().path());
    }
    // generate Feed entries
    return generator.generateFeed(entryIndexStart, entryIndexEnd);
  }

  private int getEntryIndexEnd(FetchInput fetchInput) {
    if (!Strings.isNullOrEmpty(connectorConfig.properties().path())) {
      return 0;
    } else if (!fetchInput.hasId()) {
      return connectorConfig.properties().generateProperties().entriesTotal();
    }
    return Integer.valueOf(fetchInput.getMetadata().get(ENTRY_INDEX_END).toString()) +
        connectorConfig.properties().generateProperties().entriesToAddIncremental();
  }

  private int getEntryIndexStart(FetchInput fetchInput) {
    if (!Strings.isNullOrEmpty(connectorConfig.properties().path()) || !fetchInput.hasId()) {
      return 0;
    }
    return Integer.valueOf(fetchInput.getMetadata().get(ENTRY_INDEX_START).toString()) +
        connectorConfig.properties().generateProperties().entriesToRemoveIncremental();
  }

}
