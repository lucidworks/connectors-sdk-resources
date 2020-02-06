package com.lucidworks.connectors.plugins.imap;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import com.google.inject.Inject;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.schema.ValidationError;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class ImapConfigValidator implements ValidationComponent {

  private static final Logger logger = LoggerFactory.getLogger(ImapConfigValidator.class);

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
