package com.lucidworks.fusion.connector.plugin.config;

import com.lucidworks.connector.plugins.config.RandomContentConfig;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    title = "Random Content Incremental Generator (v2)",
    description = "A connector that generates incremental random documents.",
    category = "Generator"
)
public interface RandomIncrementalConfig extends RandomContentConfig {

  @Override
  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();

  interface Properties extends RandomContentConfig.Properties {

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
