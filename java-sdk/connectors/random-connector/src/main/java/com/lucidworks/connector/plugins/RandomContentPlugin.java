package com.lucidworks.connector.plugins;

import com.google.inject.AbstractModule;
import com.lucidworks.connector.plugins.client.RandomContentGenerator;
import com.lucidworks.connector.plugins.client.impl.DefaultRandomContentGenerator;
import com.lucidworks.connector.plugins.config.RandomContentConfig;
import com.lucidworks.connector.plugins.fetcher.HostnameProvider;
import com.lucidworks.connector.plugins.fetcher.RandomContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;

import javax.inject.Inject;

import org.pf4j.PluginWrapper;

public class RandomContentPlugin extends ConnectorPluginModule {

  @Inject
  public RandomContentPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public ConnectorPlugin getConnectorPlugin() {
    AbstractModule nonGenModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(HostnameProvider.class).asEagerSingleton();
        bind(RandomContentGenerator.class)
            .to(DefaultRandomContentGenerator.class)
            .asEagerSingleton();
      }
    };
    return builder(RandomContentConfig.class)
        .withFetcher("content", RandomContentFetcher.class, nonGenModule)
        .build();
  }
}
