package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.FetcherProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.IntegerSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    name = "demo.random.content",
    title = "Random Content Generator",
    description = "A connector that generates random documents.",
    category = "Generator"
)
public interface RandomContentConfig extends ConnectorConfig<RandomContentConfig.Properties> {

  @Property(
      title = "Properties",
      required = true
  )
  public Properties getProperties();

  /**
   * Connector specific settings
   */
  interface Properties extends FetcherProperties {

    @Property(
        title = "Total",
        description = "Total number of docs to generate"
    )
    @IntegerSchema(defaultValue = 1000)
    public Integer getTotalNumDocs();

  }

}
