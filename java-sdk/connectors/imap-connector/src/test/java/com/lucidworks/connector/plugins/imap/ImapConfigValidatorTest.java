package com.lucidworks.connector.plugins.imap;

import com.google.common.collect.ImmutableMap;
import com.lucidworks.fusion.connector.plugin.api.validation.ValidationContext;
import com.lucidworks.fusion.connector.plugin.api.validation.result.ConnectorConfigValidationResult;
import com.lucidworks.fusion.schema.ModelGenerator;
import com.lucidworks.fusion.schema.SchemaGenerator;
import com.lucidworks.fusion.schema.ValidationError;
import com.lucidworks.fusion.schema.types.ObjectType;
import com.lucidworks.fusion.schema.validator.SchemaValidator;

import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ImapConfigValidatorTest {

  private ObjectType schema;
  private Map<String, Object> invalidConfig = ImmutableMap.<String, Object>builder()
      .put("id", "imap_config")
      .put("pipelineId", "pipeline_id")
      .put("properties", ImmutableMap.<String, Object>builder()
          .put("host", "-1")
          .put("username", "b")
          .put("password", "c")
          .put("ssl", true)
          .put("folder", "INBOX")
          .build())
      .build();

  @Before
  public void setup() {
    this.schema = SchemaGenerator.generate(ImapConfig.class);
  }

  @Test
  public void testRunValidateError() {
    validateSchema(invalidConfig);

    ImapConfig config = ModelGenerator.generate(ImapConfig.class, invalidConfig);

    ValidationContext context = ValidationContext.validationContextRun();

    ImapConfigValidator validator = new ImapConfigValidator(config);
    ConnectorConfigValidationResult validationResult = validator.validateConfig(context);

    Set<ValidationError> errors = validationResult.getErrors();
    assertEquals(1, errors.size());

    ValidationError firstError = errors.iterator().next();
    assertEquals("host", firstError.getField());
    assertEquals("Invalid host provided.", firstError.getMessage());
    assertEquals(0, validationResult.getWarnings().size());

  }

  @Test
  public void testCreateOrUpdateValidate() {
    validateSchema(invalidConfig);

    ImapConfig config = ModelGenerator.generate(ImapConfig.class, invalidConfig);

    ValidationContext context = ValidationContext.validationContextCreate();
    ImapConfigValidator validator = new ImapConfigValidator(config);
    ConnectorConfigValidationResult validationResult = validator.validateConfig(context);

    assertEquals(1, validationResult.getErrors().size());
  }


  private void validateSchema(Map<String, Object> data) {
    // First validate our Schema!
    SchemaValidator schemaValidator = new SchemaValidator(schema, data);
    schemaValidator.validate();
    assertEquals(schemaValidator.getValidationErrors().size(), 0);
  }
}
