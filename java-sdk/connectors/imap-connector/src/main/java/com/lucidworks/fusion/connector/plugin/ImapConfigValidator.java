package com.lucidworks.fusion.connector.plugin;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import com.google.inject.Inject;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.schema.ValidationError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashSet;
import java.util.Set;

public class ImapConfigValidator implements ValidationComponent {

  private final static Logger logger = LogManager.getLogger(ImapConfigValidator.class);

  private final ImapConfig config;

  @Inject
  public ImapConfigValidator(ImapConfig config) {
    this.config = config;
  }

  @Override
  public ConnectorConfigValidationResult validateConfig(ValidationContext validationContext) {
    logger.debug("Starting plugin configuration validation {}", config);

    Set<ValidationError> errors = new HashSet<>();

    final String host = config.properties().host();
    if (!InternetDomainName.isValid(host) && !InetAddresses.isInetAddress(host)) {
      errors.add(new ValidationError("host", host, ValidationError.INVALID_URL_VALUE, "Invalid host provided."));
    }


    // empty errors set will be interpreted as correct validation
    return ConnectorConfigValidationResult.builder(config).withErrors(errors).build();
  }
}
