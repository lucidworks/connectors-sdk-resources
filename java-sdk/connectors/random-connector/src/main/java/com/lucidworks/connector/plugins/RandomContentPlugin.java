package com.lucidworks.connector.plugins;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.shared.generator.RandomContentGenerator;
import com.lucidworks.connector.shared.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connector.shared.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.config.RandomContentConfig;
import com.lucidworks.connector.plugins.fetcher.RandomContentFetcher;
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