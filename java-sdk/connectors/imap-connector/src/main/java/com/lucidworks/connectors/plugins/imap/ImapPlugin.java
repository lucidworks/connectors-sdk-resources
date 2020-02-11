package com.lucidworks.connectors.plugins.imap;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.connectors.plugins.imap.client.ImapClient;
import com.lucidworks.connectors.plugins.imap.client.ImapStore;
import com.lucidworks.connectors.plugins.imap.client.impl.JavaxImapStore;

public class ImapPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(ImapClient.class).asEagerSingleton();
        bind(ImapStore.class)
            .to(JavaxImapStore.class)
            .asEagerSingleton();
      }
    };

    return ConnectorPlugin.builder(ImapConfig.class)
        .withFetcher("content", ImapFetcher.class, fetchModule)
        .withValidator(ImapConfigValidator.class, fetchModule)
        .build();
  }
}