package com.lucidworks.connector.plugins.security.config;

import com.lucidworks.connector.plugins.security.config.SecurityFilteringConfig.Properties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.connector.plugin.api.config.security.StaticSecurityConfig;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Security Filtering Generator (v2)",
    description =
        "A connector that generates different types of documents(Type A, B, C and D), permissions for those documents "
            + "and user/groups.",
    category = "Generator"
)
public interface SecurityFilteringConfig extends ConnectorConfig<Properties> {

  @Override
  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  interface Properties extends ConnectorPluginProperties, StaticSecurityConfig {

    @Property(
        title = "Number of Type A documents",
        description = "The number of the Type A documents to be generated",
        order = 1
    )
    @NumberSchema(defaultValue = 5, minimum = 1)
    Integer typeADocuments();

    @Property(
        title = "Number of Type B documents",
        description = "The number of the Type B documents to be generated",
        order = 2
    )
    @NumberSchema(defaultValue = 5, minimum = 1)
    Integer typeBDocuments();

    @Property(
        title = "Number of Type C documents",
        description = "The number of the Type C documents to be generated",
        order = 3
    )
    @NumberSchema(defaultValue = 5, minimum = 1)
    Integer typeCDocuments();

    @Property(
        title = "Number of Type D documents",
        description = "The number of the Type D documents to be generated",
        order = 4
    )
    @NumberSchema(defaultValue = 5, minimum = 1)
    Integer typeDDocuments();
  }
}
