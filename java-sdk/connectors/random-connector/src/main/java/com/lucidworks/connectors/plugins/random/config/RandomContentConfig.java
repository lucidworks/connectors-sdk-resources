package com.lucidworks.connectors.plugins.random.config;

import com.lucidworks.connectors.components.generator.config.RandomContentProperties;
import com.lucidworks.connectors.plugins.random.config.RandomContentConfig.Properties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Random Content Generator (v2)",
    description = "A connector that generates random documents.",
    category = "Generator"
)
public interface RandomContentConfig extends ConnectorConfig<Properties> {

  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  /**
   * Connector specific settings
   */
  interface Properties extends ConnectorPluginProperties {

    @Property(
        title = "Random Content properties",
        description = "Random Content properties"
    )
    RandomContentProperties getRandomContentProperties();
  }
}
