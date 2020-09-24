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
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilter;
import com.lucidworks.fusion.connector.plugin.api.security.SecurityFilterComponent;
import com.lucidworks.fusion.connector.plugin.api.security.Subject;

import static com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants.ACCESS_CONTROL;
import static com.lucidworks.connector.plugins.security.util.SecurityFilteringConstants.CONTENT;

public class SecurityFilteringPlugin implements ConnectorPluginProvider {

  static class MySecurityFilterComponent implements SecurityFilterComponent {

    @Override
    public SecurityFilter buildSecurityFilter(Subject subject) {
      return new SecurityFilter();
    }

  }

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
        .withSecurityFilterSpec(sf -> sf.defaultSpec(spec -> spec
            .withPrincipalFields("AC_SAM_s", "AC_UPN_s")
            .withAclFields("AC_SID_s")
            .build()))
        .withSecurityFilterSpec(sf -> sf.dynamicSpec(MySecurityFilterComponent.class, new AbstractModule() {
          @Override
          protected void configure() {
            super.configure();
          }
        }))
        .withValidator(SecurityFilteringValidationComponent.class, fetchModule)
        .build();
  }
}
