package com.lucidworks.connector.plugins.slack;

import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

public class SlackDemoPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    return ConnectorPlugin.builder(SlackDemoConfig.class)
        .withFetcher("content", SlackDemoFetcher.class)
        .build();
  }
}