package com.lucidworks.connector.plugins.incremental;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.incremental.config.RandomIncrementalConfig;
import com.lucidworks.connector.plugins.incremental.fetcher.IncrementalContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

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
