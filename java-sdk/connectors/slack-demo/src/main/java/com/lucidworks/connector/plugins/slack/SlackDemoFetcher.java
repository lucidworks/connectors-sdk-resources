package com.lucidworks.connector.plugins.slack;

import com.lucidworks.fusion.connector.plugin.api.fetcher.result.FetchResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StartResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.result.StopResult;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.content.ContentFetcher;

public class SlackDemoFetcher implements ContentFetcher {

  @Override
  public StartResult start(StartContext context) {
    return ContentFetcher.super.start(context);
  }

  @Override
  public FetchResult fetch(FetchContext context) {
    return ContentFetcher.super.fetch(context);
  }

  @Override
  public StopResult stop(StopContext context) {
    return ContentFetcher.super.stop(context);
  }
}