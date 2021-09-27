package com.lucidworks.connector.plugins.security.validation;

import com.lucidworks.connector.plugins.security.config.SecurityFilteringConfig;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.schema.ValidationError;

import javax.inject.Inject;

public class SecurityFilteringValidationComponent implements ValidationComponent {

  private static final String INVALID_VALUE_CODE = "invalid-value";

  private final SecurityFilteringConfig config;

  @Inject
  public SecurityFilteringValidationComponent(SecurityFilteringConfig config) {
    this.config = config;
  }

  @Override
  public ConnectorConfigValidationResult validateConfig(ValidationContext validationContext) {
    ConnectorConfigValidationResult.Builder builder = ConnectorConfigValidationResult.builder(config);
    if (config.properties().typeADocuments() <= 1) {
      return builder.withErrors(
          new ValidationError("Type A documents", config.properties().typeADocuments(), INVALID_VALUE_CODE))
          .build();
    }

    if (config.properties().typeBDocuments() <= 1) {
      return builder.withErrors(
          new ValidationError("Type B documents", config.properties().typeBDocuments(), INVALID_VALUE_CODE))
          .build();
    }

    if (config.properties().typeCDocuments() <= 1) {
      return builder.withErrors(
          new ValidationError("Type C documents", config.properties().typeCDocuments(), INVALID_VALUE_CODE))
          .build();
    }

    if (config.properties().typeDDocuments() <= 1) {
      return builder.withErrors(
          new ValidationError("Type D documents", config.properties().typeDDocuments(), INVALID_VALUE_CODE))
          .build();
    }

    return builder.build();
  }
}