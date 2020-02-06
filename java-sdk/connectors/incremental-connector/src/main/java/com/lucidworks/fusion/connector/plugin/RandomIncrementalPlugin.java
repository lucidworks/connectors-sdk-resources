package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidowkrs.connector.shared.generator.RandomContentGenerator;
import com.lucidowkrs.connector.shared.generator.impl.DefaultRandomContentGenerator;
import com.lucidowkrs.connector.shared.hostname.HostnameProvider;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.fusion.connector.plugin.config.RandomIncrementalConfig;
import com.lucidworks.fusion.connector.plugin.fetcher.IncrementalContentFetcher;

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
