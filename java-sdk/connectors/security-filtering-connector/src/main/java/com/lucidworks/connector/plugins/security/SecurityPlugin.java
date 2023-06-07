package com.lucidworks.connector.plugins.security;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.plugins.security.fetcher.SecurityFilteringAccessControlFetcher;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

public class SecurityPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
      }
    };

    return ConnectorPlugin.builder(SecurityConfig.class)
        .withFetcher("access-control", SecurityFilteringAccessControlFetcher.class, fetchModule)
        .withSecuritySpec(sf -> sf
            .staticSpec(spec -> spec
                .withPrincipal("fullName_s")))
        .build();
  }
}
