package com.lucidworks.fusion.connector.plugin.feed;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;

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
