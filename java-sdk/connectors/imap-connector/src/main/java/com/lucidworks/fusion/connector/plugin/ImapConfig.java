package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.ConnectorPluginProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.BooleanSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.UIHints;

@RootSchema(
    title = "IMAP (v2)",
    description = "An IMAP connector",
    category = "Email"
)
public interface ImapConfig extends ConnectorConfig<ImapConfig.Properties> {

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
        title = "Host",
        description = "The hostname of the IMAP server",
        required = true,
        order = 0
    )
    @StringSchema
    String host();

    @Property(
        title = "Username",
        description = "The username",
        required = true,
        order = 1
    )
    @StringSchema
    String username();

    @Property(
        title = "Password",
        description = "The password",
        required = true,
        order = 2,
        hints = {UIHints.SECRET}
    )
    @StringSchema
    String password();

    @Property(
        title = "SSL",
        description = "Connect securely using SSL",
        required = true,
        order = 3
    )
    @BooleanSchema
    boolean ssl();

    @Property(
        title = "Folder",
        description = "The folder to retrieve messages from",
        required = true,
        order = 4
    )
    @StringSchema(defaultValue = "Inbox")
    String folder();
  }

}
