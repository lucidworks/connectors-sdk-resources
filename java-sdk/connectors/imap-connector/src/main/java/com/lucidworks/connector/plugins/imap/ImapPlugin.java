package com.lucidworks.connector.plugins.imap;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.plugins.imap.client.ImapClient;
import com.lucidworks.connector.plugins.imap.client.ImapStore;
import com.lucidworks.connector.plugins.imap.client.impl.JavaxImapStore;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

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