package com.lucidworks.connectors.plugins.feed;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.connectors.plugins.feed.config.FeedConfig;
import com.lucidworks.connectors.plugins.feed.feed.DefaultFeedGenerator;
import com.lucidworks.connectors.plugins.feed.feed.FeedGenerator;
import com.lucidworks.connectors.plugins.feed.fetcher.FeedFetcher;

public class FeedPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(FeedGenerator.class)
            .to(DefaultFeedGenerator.class)
            .asEagerSingleton();
      }
    };

    return ConnectorPlugin.builder(FeedConfig.class)
        .withFetcher("content", FeedFetcher.class, fetchModule)
        .build();
  }
}