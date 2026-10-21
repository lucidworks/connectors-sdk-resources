package com.lucidworks.connector.plugins.changeonlycrawler;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.incremental.config.RandomIncrementalConfig;
import com.lucidworks.connector.plugins.incremental.fetcher.IncrementalContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin.FetcherDef;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

/**
 * Same fetcher and config as the incremental connector, but the fetcher is not marked
 * {@code FetcherType.INCREMENTAL}, so it runs as a CRAWLER that emits only changes after its checkpoint.
 */
public class ChangeOnlyCrawlerPlugin implements ConnectorPluginProvider {

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
        .withFetcher(FetcherDef.builder("content", IncrementalContentFetcher.class, fetchModule)
            .build())
        .build();
  }

}
