package com.lucidworks.fusion.connector.plugin.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

@RootSchema(
    title = "Feed Random Connector (v2)",
    description = "A simple feed connector",
    category = "Feed"
)
public interface FeedConfig extends ConnectorConfig<FeedConfig.Properties> {

  /**
   * Connector specific settings
   */
  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  /**
   * Connector specific settings
   */
  interface Properties extends ConnectorPluginProperties, GenerateConfig {

    @Property(
        title = "Feed file path",
        description = "Feed file path location. If empty, the connector will generate entries (see 'Generate Properties')"
    )
    @StringSchema
    String path();

  }

}
