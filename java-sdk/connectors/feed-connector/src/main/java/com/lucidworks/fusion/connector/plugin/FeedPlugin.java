package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.fusion.connector.plugin.config.FeedConfig;
import com.lucidworks.fusion.connector.plugin.feed.DefaultFeedGenerator;
import com.lucidworks.fusion.connector.plugin.feed.FeedGenerator;
import com.lucidworks.fusion.connector.plugin.fetcher.FeedFetcher;

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