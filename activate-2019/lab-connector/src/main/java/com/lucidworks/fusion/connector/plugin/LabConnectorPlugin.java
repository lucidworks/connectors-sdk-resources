package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;
import com.lucidworks.fusion.connector.plugin.impl.DefaultGenerator;
import javax.inject.Inject;
import org.pf4j.PluginWrapper;

public class LabConnectorPlugin extends ConnectorPluginModule {

  @Inject
  public LabConnectorPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public ConnectorPlugin getConnectorPlugin() {
    Module module = new AbstractModule() {
      @Override
      protected void configure() {
        bind(LabConnectorGenerator.class).toInstance(new DefaultGenerator());
      }
    };
    return builder(LabConnectorConfig.class)
        .withFetcher("content", LabConnectorFetcher.class, module)
        .build();
  }
}
