package com.lucidworks.fusion.connector.plugin;

import com.lucidworks.fusion.connector.plugin.api.config.ConnectorConfig;
import com.lucidworks.fusion.connector.plugin.api.config.FetcherProperties;
import com.lucidworks.fusion.schema.SchemaAnnotations.BooleanSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.Property;
import com.lucidworks.fusion.schema.SchemaAnnotations.RootSchema;
import com.lucidworks.fusion.schema.SchemaAnnotations.StringSchema;
import com.lucidworks.fusion.schema.UIHints;

@RootSchema(
    name = "demo.imap",
    title = "IMAP",
    description = "An IMAP connector",
    category = "Email"
)
public interface ImapConfig extends ConnectorConfig<ImapConfig.Properties> {

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
        title = "Host",
        description = "The hostname of the IMAP server",
        required = true,
        order = 0
    )
    @StringSchema
    public String getHost();

    @Property(
        title = "Username",
        description = "The username",
        required = true,
        order = 1
    )
    @StringSchema
    public String getUsername();

    @Property(
        title = "Password",
        description = "The password",
        required = true,
        order = 2,
        hints = {UIHints.SECRET}
    )
    @StringSchema
    public String getPassword();

    @Property(
        title = "SSL",
        description = "Connect securely using SSL",
        required = true,
        order = 3
    )
    @BooleanSchema
    public boolean getSsl();

    @Property(
        title = "Folder",
        description = "The folder to retrieve messages from",
        required = true,
        order = 4
    )
    @StringSchema(defaultValue = "Inbox")
    public String getFolder();
  }

}
