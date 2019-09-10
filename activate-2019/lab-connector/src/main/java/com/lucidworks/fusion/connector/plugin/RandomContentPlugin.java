package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginModule;
import javax.inject.Inject;
import org.pf4j.PluginWrapper;

public class RandomContentPlugin extends ConnectorPluginModule {

  @Inject
  public RandomContentPlugin(PluginWrapper wrapper) {
    super(wrapper);
  }

  @Override
  public ConnectorPlugin getConnectorPlugin() {
    return builder(RandomContentConfig.class).build();
  }
}
