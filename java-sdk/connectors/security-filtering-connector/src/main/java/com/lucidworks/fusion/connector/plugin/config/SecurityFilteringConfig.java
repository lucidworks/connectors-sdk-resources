package com.lucidworks.fusion.connector.plugin.config;

import com.lucidworks.fusion.connector.plugin.RandomContentConfig;
import com.lucidworks.fusion.connector.plugin.api.config.SecurityTrimmingConfig;
import com.lucidworks.fusion.schema.SchemaAnnotations.NumberSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;

@RootSchema(
    name = "demo.security.filtering",
    title = "Security Filtering Generator",
    description = "A connector that generates random documents, document ACLs and access controls.",
    category = "Generator"
)
public interface SecurityFilteringConfig extends RandomContentConfig {
  
  @Override
  @Property(
      title = "Properties",
      required = true
  )
  Properties properties();
  
  interface Properties extends RandomContentConfig.Properties, SecurityTrimmingConfig {
    @Property(
        title = "Nested groups",
        description = "Number of nested groups"
    )
    @NumberSchema(defaultValue = 10, minimum = 1)
    Integer numberOfNestedGroups();
  }
}
