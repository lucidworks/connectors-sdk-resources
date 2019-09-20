package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;
import com.lucidworks.fusion.connector.plugin.config.RandomIncrementalConfig;
import com.lucidworks.fusion.connector.plugin.fetcher.RandomInContentIncrementalFetcher;
import com.lucidworks.fusion.connector.plugin.impl.DefaultRandomContentGenerator;
import org.pf4j.PluginWrapper;

import javax.inject.Inject;

public class RandomIncrementalPlugin extends ConnectorPluginModule {

  @Inject
  public RandomIncrementalPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public ConnectorPlugin getConnectorPlugin() {
    AbstractModule module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(RandomContentGenerator.class)
            .to(DefaultRandomContentGenerator.class)
            .asEagerSingleton();
      }
    };
    return builder(RandomIncrementalConfig.class)
        .withFetcher("content", RandomInContentIncrementalFetcher.class, module)
        .build();
  }
}
