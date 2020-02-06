package com.lucidworks.connector.shared.generator.config;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;

public interface RandomContentProperties extends Model {
  @Property(
      title = "Total",
      description = "Total number of docs to generate",
      order = 1
  )
  @NumberSchema(defaultValue = 1000)
  Integer totalNumDocs();

  @Property(
      title = "Minimum number of Sentences",
      description = "The connector Generates a Minimum number of Sentences. (10 by default)",
      order = 2
  )
  @NumberSchema(defaultValue = 10)
  Integer minimumNumberSentences();

  @Property(
      title = "Maximum number of Sentences",
      description = "The connector Generates Maximum number of Sentences. (255 by default)",
      order = 3
  )
  @NumberSchema(defaultValue = 255)
  Integer maximumNumberSentences();
}