package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;
import com.lucidworks.fusion.connector.plugin.impl.DefaultRandomContentGenerator;
import org.pf4j.PluginWrapper;

import javax.inject.Inject;

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
        bind(RandomContentGenerator.class)
            .to(DefaultRandomContentGenerator.class)
            .asEagerSingleton();
      }
    };
    return builder(RandomContentConfig.class)
        .withFetcher(RandomContentFetcher.class, nonGenModule)
        .build();
  }
}
