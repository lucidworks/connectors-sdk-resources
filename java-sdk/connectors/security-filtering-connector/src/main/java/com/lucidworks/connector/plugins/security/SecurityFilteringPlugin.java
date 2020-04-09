package com.lucidworks.connector.plugins.security;

import com.google.inject.AbstractModule;
import com.google.inject.Module;
import com.lucidworks.connector.components.generator.RandomContentGenerator;
import com.lucidworks.connector.components.generator.impl.DefaultRandomContentGenerator;
import com.lucidworks.connector.plugins.security.config.SecurityFilteringConfig;
import com.lucidworks.connector.plugins.security.fetcher.SecurityFilteringAccessControlFetcher;
import com.lucidworks.connector.plugins.security.fetcher.SecurityFilteringContentFetcher;
import com.lucidworks.connector.plugins.security.validation.SecurityFilteringValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.connector.plugins.security.component.ExampleSecurityFilterFieldsComponent;

import static com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants.ACCESS_CONTROL;
import static com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants.CONTENT;

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
        .withSecurityFilterFields(ExampleSecurityFilterFieldsComponent.class)
        .withValidator(SecurityFilteringValidationComponent.class, fetchModule)
        .build();
  }
}
