package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    name = "demo.random.content",
    title = "Random Content Generator",
    description = "A connector that generates random documents.",
    category = "Generator"
)
public interface LabConnectorConfig extends ConnectorConfig<LabConnectorConfig.Properties> {

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
        title = "Total",
        description = "Total number of docs to generate"
    )
    @NumberSchema(defaultValue = 1000)
    Integer totalNumDocs();
  }
}
