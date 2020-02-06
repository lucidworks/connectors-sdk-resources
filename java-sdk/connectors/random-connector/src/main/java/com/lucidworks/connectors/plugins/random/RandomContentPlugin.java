package com.lucidworks.connectors.plugins.random;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connectors.components.generator.RandomContentGenerator;
import com.lucidworks.connectors.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connectors.components.hostname.HostnameProvider;
import com.lucidworks.connectors.plugins.random.config.RandomContentConfig;
import com.lucidworks.connectors.plugins.random.fetcher.RandomContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

public class RandomContentPlugin implements ConnectorPluginProvider {

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

    return ConnectorPlugin.builder(RandomContentConfig.class)
        .withFetcher("content", RandomContentFetcher.class, fetchModule)
        .build();
  }
}