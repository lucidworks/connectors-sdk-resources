package com.lucidworks.fusion.connector.plugin.validation;

import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import com.lucidworks.fusion.schema.ValidationError;

import javax.inject.Inject;
import java.util.function.Predicate;

public class SecurityFilteringValidationComponent implements ValidationComponent {

  private final SecurityFilteringConfig config;

  @Inject
  public SecurityFilteringValidationComponent(SecurityFilteringConfig config) {
    this.config = config;
  }

  @Override
  public ConnectorConfigValidationResult validateConfig(ValidationContext validationContext) {
    ConnectorConfigValidationResult.Builder builder = ConnectorConfigValidationResult.builder(config);
    Predicate<SecurityFilteringConfig.Properties> predicate = p -> p.getRandomContentProperties().totalNumDocs() % p.numberOfNestedGroups() != 0;

    if (validationContext.isCreateOrUpdate() && predicate.test(config.properties())) {
      builder.withErrors(new ValidationError(
          "numberOfNestedGroups",
          config.properties().numberOfNestedGroups(),
          null,
          "Module between totalNumDocs and numberOfNestedGroups must be zero"
      ));
    }
    return builder.build();
  }
}