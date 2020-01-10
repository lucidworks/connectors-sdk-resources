package com.lucidworks.fusion.connector.plugin.config;

import com.lucidowkrs.connector.shared.generator.config.RandomContentProperties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.connector.plugin.api.config.SecurityTrimmingConfig;
import com.lucidworks.fusion.connector.plugin.config.SecurityFilteringConfig.Properties;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Security Filtering Generator (v2)",
    description = "A connector that generates random documents, document ACLs and access controls.",
    category = "Generator"
)
public interface SecurityFilteringConfig extends ConnectorConfig<Properties> {

  @Override
  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  interface Properties extends ConnectorPluginProperties, SecurityTrimmingConfig {
    @Property(
        title = "Nested groups",
        description = "Number of nested groups",
        order = 1
    )
    @NumberSchema(defaultValue = 10, minimum = 1)
    Integer numberOfNestedGroups();

    @Property(
        title = "Random Content properties",
        description = "Random Content properties",
        order = 2
    )
    RandomContentProperties getRandomContentProperties();
  }
}
