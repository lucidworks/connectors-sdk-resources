package com.lucidworks.fusion.connector.plugin;

import com.google.common.net.InetAddresses;
import com.google.common.net.InternetDomainName;
import com.google.inject.Inject;
import com.lucidworks.fusion.connector.plugin.api.config.InvalidConnectorConfigException;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationComponent;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationResult;
import com.lucidworks.fusion.schema.ValidationError;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ImapConfigValidator implements ValidationComponent {

  private final static Logger logger = LogManager.getLogger(ImapConfigValidator.class);

  private final ImapConfig config;

  @Inject
  public ImapConfigValidator(
      ImapConfig config
  ) {
    this.config = config;
  }

  @Override
  public ValidationResult validateConfig(ValidationContext validationContext) {
    List<ValidationError> errors = new ArrayList<>();

    final String host = config.getProperties().getHost();

    if (!InternetDomainName.isValid(host) && !InetAddresses.isInetAddress(host)) {
      errors.add(new ValidationError("host", null, "Invalid host provided."));
    }

    if (!errors.isEmpty()) {
      return ValidationResult.builder(config).withErrors(errors).build();
    }

    return ValidationResult.builder(config).build();
  }
}
