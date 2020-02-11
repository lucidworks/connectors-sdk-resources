package com.lucidworks.connectors.plugins.Incremental;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connectors.components.generator.RandomContentGenerator;
import com.lucidworks.connectors.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connectors.components.hostname.HostnameProvider;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.connectors.plugins.Incremental.config.RandomIncrementalConfig;
import com.lucidworks.connectors.plugins.Incremental.fetcher.IncrementalContentFetcher;

public class RandomIncrementalPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(HostnameProvider.class).asEagerSingleton();
        bind(RandomContentGenerator.class)
            .to(DefaultRandomContentGenerator.class)
            .asEagerSingleton();
      }
    };

    return ConnectorPlugin.builder(RandomIncrementalConfig.class)
        .withFetcher("content", IncrementalContentFetcher.class, fetchModule)
        .build();
  }

}
