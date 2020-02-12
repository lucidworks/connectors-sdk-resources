package com.lucidworks.connectors.plugins.security;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connectors.components.generator.RandomContentGenerator;
import com.lucidworks.connectors.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connectors.plugins.security.config.SecurityFilteringConfig;
import com.lucidworks.connectors.plugins.security.fetcher.SecurityFilteringAccessControlFetcher;
import com.lucidworks.connectors.plugins.security.fetcher.SecurityFilteringContentFetcher;
import com.lucidworks.connectors.plugins.security.validation.SecurityFilteringValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;

import static com.lucidworks.connectors.plugins.security.util.SecurityFilteringConstants.ACCESS_CONTROL;
import static com.lucidworks.connectors.plugins.security.util.SecurityFilteringConstants.CONTENT;

public class SecurityFilteringPlugin implements ConnectorPluginProvider {

  @Override
  public ConnectorPlugin get() {
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(RandomContentGenerator.class)
            .to(DefaultRandomContentGenerator.class)
            .asEagerSingleton();
      }
    };

    return ConnectorPlugin.builder(SecurityFilteringConfig.class)
        .withFetcher(CONTENT, SecurityFilteringContentFetcher.class, fetchModule)
        .withFetcher(ACCESS_CONTROL, SecurityFilteringAccessControlFetcher.class, fetchModule)
        .withManagedFilters("AC_SAM_s")
        .withValidator(SecurityFilteringValidationComponent.class, fetchModule)
        .build();
  }
}
