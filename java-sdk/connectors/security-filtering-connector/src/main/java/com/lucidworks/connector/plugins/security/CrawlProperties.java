package com.lucidworks.connector.plugins.security;

import com.lucidworks.fusion.schema.Model;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;

public interface CrawlProperties extends Model {
  @Property(
      title = "Acl Document Id",
      description = "Valid Acl document Id for delete"
  )
  @StringSchema(defaultValue = "NA")
  String acId();

  @Property(
      title = "CRUD Operation",
      description = "Create or Delete"
  )
  @StringSchema(defaultValue = "create")
  String crudOperation();

}
