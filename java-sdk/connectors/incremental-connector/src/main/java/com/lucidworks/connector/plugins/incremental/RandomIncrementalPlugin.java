package com.lucidworks.connector.plugins.incremental;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connector.components.hostname.HostnameProvider;
import com.lucidworks.connector.plugins.incremental.config.RandomIncrementalConfig;
import com.lucidworks.connector.plugins.incremental.fetcher.IncrementalContentFetcher;
import com.lucidworks.fusion.connector.plugin.api.fetcher.type.FetcherType;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin.FetcherDef;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Properties;

public class RandomIncrementalPlugin implements ConnectorPluginProvider {

  private static final String FETCHER_TYPE_RESOURCE = "/incremental-demo-fetcher-type.properties";

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

    FetcherDef.Builder fetcher = FetcherDef.builder("content", IncrementalContentFetcher.class, fetchModule);
    if (isIncremental()) {
      fetcher.withFetcherType(FetcherType.INCREMENTAL);
    }

    return ConnectorPlugin.builder(RandomIncrementalConfig.class)
        .withFetcher(fetcher.build())
        .build();
  }

  // Set at build time by -PdemoFetcherType; "crawler" leaves the fetcher without the INCREMENTAL marker.
  private static boolean isIncremental() {
    try (InputStream in = RandomIncrementalPlugin.class.getResourceAsStream(FETCHER_TYPE_RESOURCE)) {
      if (in == null) {
        return true;
      }
      Properties props = new Properties();
      props.load(in);
      return !"crawler".equals(props.getProperty("fetcherType"));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

}
