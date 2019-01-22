package com.lucidworks.fusion.connector.plugin;

import com.google.common.collect.ImmutableMap;
import com.lucidworks.fusion.schema.ModelGenerator;
import com.lucidworks.fusion.schema.SchemaGenerator;
import com.lucidworks.fusion.schema.types.AnyType;
import com.lucidworks.fusion.schema.types.ObjectType;
import org.codehaus.jackson.JsonProcessingException;
import org.junit.Test;

import java.util.Map;

import static org.junit.Assert.*;

public class ImapConfigTest {
  @Test
  public void testSchemaProperties() throws JsonProcessingException {
    ObjectType schema = SchemaGenerator.generate(ImapConfig.class);
    assertTrue(schema.getProperties().containsKey("properties"));
    AnyType properties = schema.getProperties().get("properties");
    assertTrue(properties instanceof ObjectType);

    assertTrue(
        ((ObjectType) properties).getProperties().containsKey("host")
    );
    assertTrue(
        ((ObjectType) properties).getProperties().containsKey("username")
    );
    assertTrue(
        ((ObjectType) properties).getProperties().containsKey("password")
    );
    assertTrue(
        ((ObjectType) properties).getProperties().containsKey("ssl")
    );
    assertTrue(
        ((ObjectType) properties).getProperties().containsKey("folder")
    );

    assertFalse(
        ((ObjectType) properties).getProperties().get("host").getDescription().isEmpty()
    );
    assertFalse(
        ((ObjectType) properties).getProperties().get("username").getDescription().isEmpty()
    );
    assertFalse(
        ((ObjectType) properties).getProperties().get("password").getDescription().isEmpty()
    );
    assertFalse(
        ((ObjectType) properties).getProperties().get("ssl").getDescription().isEmpty()
    );
    assertFalse(
        ((ObjectType) properties).getProperties().get("folder").getDescription().isEmpty()
    );
  }

  @Test
  public void testConfigProperties() {
    String host = "imap.hostname.com";
    String username = "jdoe";
    String password = "topsecret";
    boolean ssl = false;
    String folder = "Inbox";


    Map<String, Object> data = ImmutableMap.<String, Object>builder()
        .put("properties", ImmutableMap.<String, Object>builder()
            .put("host", host)
            .put("username", username)
            .put("password", password)
            .put("ssl", ssl)
            .put("folder", folder).build())
        .build();

    ImapConfig config = ModelGenerator.generate(ImapConfig.class, data);
    assertEquals(
        host,
        config.getProperties().getHost());
    assertEquals(
        username,
        config.getProperties().getUsername());
    assertEquals(
        password,
        config.getProperties().getPassword());
    assertEquals(
        ssl,
        config.getProperties().getSsl());
    assertEquals(
        folder,
        config.getProperties().getFolder());


  }
}
