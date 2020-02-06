package com.lucidworks.connectors.plugins.feed.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.ObjectSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

public interface GenerateConfig extends Model {

  /**
   * @return an instance of {@link Properties}.
   */
  @Property
  Properties generateProperties();

  @ObjectSchema(
      title = "Generate Properties",
      description = "Generates entries when a feed file is not provided."
  )
  interface Properties extends Model {

    @Property(
        title = "Total to generate first crawl",
        description = "Total number of total entries to generate in the first crawl. It will be ignored if 'Feed file" +
            " path' is provided"
    )
    @NumberSchema(
        defaultValue = 1000
    )
    Integer entriesTotal();

    @Property(
        title = "Total incremental to remove",
        description = "Total number of entries to remove from the second and subsequent crawls. It will be ignored if" +
            " 'Feed file path' is provided"
    )
    @NumberSchema(
        defaultValue = 100
    )
    Integer entriesToRemoveIncremental();

    @Property(
        title = "Total incremental to add",
        description = "Total number of entries to add from the second and subsequent crawls. It will be ignored if " +
            "'Feed file path' is provided"
    )
    @NumberSchema(
        defaultValue = 5
    )
    Integer entriesToAddIncremental();
  }
}
