package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;
import com.lucidworks.fusion.connector.plugin.config.FeedConfig;
import com.lucidworks.fusion.connector.plugin.feed.DefaultFeedGenerator;
import com.lucidworks.fusion.connector.plugin.feed.FeedGenerator;
import com.lucidworks.fusion.connector.plugin.fetcher.FeedFetcher;
import org.pf4j.PluginWrapper;

import javax.inject.Inject;

public class FeedPlugin extends ConnectorPluginModule {

  @Inject
  public FeedPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public ConnectorPlugin getConnectorPlugin() {
    AbstractModule module = new AbstractModule() {
      @Override
      protected void configure() {

        bind(FeedGenerator.class)
            .to(DefaultFeedGenerator.class)
            .asEagerSingleton();
      }
    };
    return builder(FeedConfig.class)
        .withFetcher("content", FeedFetcher.class, module)
        .build();
  }
}
