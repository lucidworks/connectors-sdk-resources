package com.lucidworks.fusion.connector.plugin.validation;

import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import com.lucidworks.fusion.schema.ValidationError;

import javax.inject.Inject;

public class SecurityFilteringValidationComponent implements ValidationComponent {

  private final SecurityFilteringConfig config;

  @Inject
  public SecurityFilteringValidationComponent(SecurityFilteringConfig config) {
    this.config = config;
  }


  @Override
  public ConnectorConfigValidationResult validateConfig(ValidationContext validationContext) {
    ConnectorConfigValidationResult.Builder builder = ConnectorConfigValidationResult.builder(config);
    if (config.properties().totalNumDocs() % config.properties().numberOfNestedGroups() != 0) {
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
