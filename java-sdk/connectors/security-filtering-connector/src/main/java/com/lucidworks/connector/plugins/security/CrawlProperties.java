package com.lucidworks.connector.plugins.security;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

public interface CrawlProperties extends Model {
  @Property(
      title = "AC Id",
      description = "AC Id for update or delete"
  )
  @StringSchema
  String acId();

  @Property(
      title = "CRUD Operation",
      description = "Create, Update or Delete"
  )
  @StringSchema
  String crudOperation();

}
