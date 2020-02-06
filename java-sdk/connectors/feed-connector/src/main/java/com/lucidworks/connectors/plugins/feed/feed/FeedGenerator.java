package com.lucidworks.connectors.plugins.feed.feed;

import java.util.Map;

public interface FeedGenerator {

  Feed readFeed(String path);

  Map<String, Object> generateFieldsMap();

  Feed generateFeed(int entryIndexStart, int entryIndexEnd);

}
