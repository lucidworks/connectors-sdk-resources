package com.lucidworks.fusion.connector.plugin;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.shared.generator.RandomContentGenerator;
import com.lucidworks.connector.shared.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import com.lucidworks.fusion.connector.plugin.fetcher.SecurityFilteringAccessControlFetcher;
import com.lucidworks.fusion.connector.plugin.fetcher.SecurityFilteringContentFetcher;
import com.lucidworks.fusion.connector.plugin.security.SecurityFilteringSecurityFilterComponent;
import com.lucidworks.fusion.connector.plugin.validation.SecurityFilteringValidationComponent;

import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.ACCESS_CONTROL;
import static com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants.CONTENT;

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
        .withSecurityFilter(SecurityFilteringSecurityFilterComponent.class, fetchModule)
        .withValidator(SecurityFilteringValidationComponent.class, fetchModule)
        .build();
  }
}