package com.lucidworks.connector.plugins.slack;

import com.lucidworks.connector.plugins.slack.SlackDemoConfig.Properties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Slack connector demo",
    description = "Slack connector demo",
    category = "demo"
)
public interface SlackDemoConfig extends ConnectorConfig<Properties> {

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
        title = "Slack App token"
    )
    String token();

    @Property(
        title = "Slack Page limit"
    )
    @NumberSchema(
        defaultValue = 200,
        minimum = 1,
        maximum = 1000
    )
    Integer pageLimit();
  }
}