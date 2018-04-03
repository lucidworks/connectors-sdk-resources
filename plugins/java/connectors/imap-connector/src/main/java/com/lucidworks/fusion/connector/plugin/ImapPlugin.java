package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;
import org.pf4j.PluginWrapper;

import javax.inject.Inject;

public class ImapPlugin extends ConnectorPluginModule {
  @Inject
  public ImapPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public ConnectorPlugin getConnectorPlugin() {
    AbstractModule module = new AbstractModule() {
      @Override
      protected void configure() {

      }
    };
    return builder(ImapConfig.class)
        .withFetcher(ImapFetcher.class, module)
//        .withValidator(ImapConfigValidator.class)
        .build();
  }
}
