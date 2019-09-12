package com.lucidworks.fusion.connector.plugin.config;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

@RootSchema(
    name = "feed-random",
    title = "Feed Random Connector",
    description = "A feed-random connector",
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
  interface Properties extends ConnectorPluginProperties {

    @Property(
        title = "Feed file path",
        description = "Feed file path location",
        required = true,
        order = 1
    )
    @StringSchema
    String path();
  }
}
