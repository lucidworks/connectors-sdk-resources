package com.lucidworks.connector.plugins.feed.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.stream.IntStream;

public class DefaultFeedGenerator implements FeedGenerator {

  private final ObjectMapper mapper = new ObjectMapper();

  @Override
  public Feed readFeed(String path) {
    try {
      byte[] encoded = Files.readAllBytes(Paths.get(path));
      String content = new String(encoded, StandardCharsets.UTF_8);
      return mapper.readValue(content, Feed.class);
    } catch (IOException e) {
      throw new RuntimeException(String.format("Error reading feed file %s", path), e);
    }
  }

  @Override
  public Feed generateFeed(int entryIndexStart, int entryIndexEnd) {
    ImmutableMap.Builder<String, FeedEntry> builder = ImmutableMap.builder();
    // generating entries from 'entryIndexStart'.
    // Entries not generated before 'entryIndexStart' will not be emitted as candidates, this will simulate when
    // entries are removed from the Feed
    IntStream.range(entryIndexStart, entryIndexEnd).asLongStream().forEach(index -> {
      builder.put(
          String.valueOf(index),
          new FeedEntry(String.valueOf(index), UUID.randomUUID().toString(), Instant.now().toEpochMilli())
      );
    });
    return new Feed(builder.build());
  }

  @Override
  public Map<String, Object> generateFieldsMap() {
    return ImmutableMap.<String, Object>builder()
        .put("abc_s", UUID.randomUUID().toString())
        .put("xyz_s", UUID.randomUUID().toString())
        .put("qwe_s", UUID.randomUUID().toString())
        .put("dfg_s", UUID.randomUUID().toString())
        .put("ghy_s", UUID.randomUUID().toString())
        .put("uik_s", UUID.randomUUID().toString())
        .put("awf_s", UUID.randomUUID().toString())
        .put("tyu_s", UUID.randomUUID().toString())
        .build();
  }

}
