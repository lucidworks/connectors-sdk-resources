package com.lucidworks.connector.plugins.security;

import com.lucidworks.connector.plugins.security.SecurityConfig.Properties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.connector.plugin.api.config.security.GraphSecurityConfig;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Security Demo Connector",
    description = "A test connector for integration tests.",
    category = "Test connector"
)
public interface SecurityConfig extends ConnectorConfig<Properties> {

  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  /**
   * Connector specific settings
   */
  interface Properties extends ConnectorPluginProperties, GraphSecurityConfig {


    @Property(
        title = "Crawl Properties",
        description = "Crawl Properties"
    )
    CrawlProperties crawlProperties();

  }
}
