package com.lucidworks.fusion.connector.plugin.validation;

import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig;
import com.lucidworks.fusion.connector.plugin.util.SecurityFilteringConstants;
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
    if (config.properties().typeADocuments() <= 0) {
      return builder.withErrors(
          new ValidationError("Type A documents", config.properties().typeADocuments(), null,
              SecurityFilteringConstants.INVALID_VALUE)).build();
    }

    if (config.properties().typeBDocuments() <= 0) {
      return builder.withErrors(
          new ValidationError("Type B documents", config.properties().typeBDocuments(), null,
              SecurityFilteringConstants.INVALID_VALUE)).build();
    }

    if (config.properties().typeCDocuments() <= 0) {
      return builder.withErrors(
          new ValidationError("Type C documents", config.properties().typeCDocuments(), null,
              SecurityFilteringConstants.INVALID_VALUE)).build();
    }

    if (config.properties().typeDDocuments() <= 0) {
      return builder.withErrors(
          new ValidationError("Type D documents", config.properties().typeDDocuments(), null,
              SecurityFilteringConstants.INVALID_VALUE)).build();
    }

    return builder.build();
  }
}