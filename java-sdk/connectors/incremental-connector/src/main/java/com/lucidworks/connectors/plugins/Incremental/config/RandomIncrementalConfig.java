package com.lucidworks.connectors.plugins.Incremental.config;

import com.lucidworks.connectors.components.generator.config.RandomContentProperties;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.connectors.plugins.Incremental.config.RandomIncrementalConfig.Properties;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Random Content Incremental Generator (v2)",
    description = "A connector that generates incremental random documents.",
    category = "Generator"
)
public interface RandomIncrementalConfig extends ConnectorConfig<Properties> {

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

    @Property(
        title = "Incremental Total",
        description = "Total number of docs to generate from the second and subsequent crawls."
    )
    @NumberSchema(
        defaultValue = 10
    )
    Integer totalNumDocsIncremental();
  }
}
