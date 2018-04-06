# Connector Java SDK Configuration

In order to build a valid Connector configuration, you must:
* Define an interface
* Extend `ConnectorConfig`
* Apply a few annotations
* Define Connector "getter" methods + annotations

All methods that are annotated with `@Property` and start with "get" are considered to be configuration properties.
For example, `@Property() String getName();` would result in a String property called `name`.
This property would then be present in the generated schema.

Here is an example of the most basic configuration, along with required annotations and a sample "getter" method:

```java
@RootSchema(
    name = "com.my.connector",
    title = "My Connector",
    description = "My Connector description",
    category = "My Category"
)
public interface MyConfig extends ConnectorConfig<MyConfig.Properties> {

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
        title = "My custom property",
        description = "My custom property description"
    )
    public Integer getMyCustomProperty();

  }

}

```

The metadata defined by `@RootSchema` is used by Fusion when showing the list of available Connectors.
The `ConnectorConfig` base interface represents common, top-level settings required by all Connectors.
The type parameter of the `ConnectorConfig` class indicates the interface to use for custom properties.

Once a Connector configuration has been defined, it can be associated with the ConnectorPlugin class.
From that point, the framework takes care of providing the configuration instances to your Connector.
It also takes care of generating the schema, and sending it along to Fusion when it connects to Fusion.

## Additiona Info on com.lucidworks.schema
The `com.lucidworks.schema` project (included in the Connectors SDK) aims to make the process of creating JSON Schemas simple. It also provides utilities for building type safe POJOs from schema definitions.

The basic idea here is that you create an interface that extends `Model`, add property getters and then a few annotations. Once your interface has been built, you can generate an `ObjectType` instance, which is the object that contains all of the JSON schema metadata. By then combining that schema object with a `Map<String,Object>`, you can create instances of your interface to use as configuration objects.

The configuration objects are based on `java.lang.reflect.Proxy`, which is what is returned by the `ModelGenerator`. Most method calls to these Proxy instances result method call to the underlying `Map<String,Object>`. For example, if the interface defined a `getId` method, a runtime call would result in call to the `Map<String,Object>`: `data.get("id")`.

A few special cases exist as well. `toString` is proxied directly to the `Map<String,Object>`. `getClass` returns the class of the interface provided to `ModelGenerator#generate()`. `_data` returns the underlying `Map<String,Object>` object. If the method starts with `set`, the arguments are captured and sent to the underlying map via `put()`.

Here's a simple example:

```java
interface MyConfig extends Model {
  
  @Property
  @StringSchema(minLength=1)
  String getId();
  
}
```

Now to create the schema:

```java
class Runner {
  public static void main(String[] args){
    ObjectType schema = SchemaGenerator.generate(MyConfig.class);
  }
}
```

To generate an instance of the `MyConfig` class:

```java
class Runner {
  public static void main(String[] args){
    Map<String,Object> data = new java.util.HashMap<>();
    data.put("id", 100);
    MyConfig config = ModelGenerator.generate(MyConfig.class, data);
    System.out.println("id -> " + config.getId());
  }
}
```

Schema metadata can be applied to properties using additional annotations. For example, applying limits to the min/max length of a String, or describing the types of items in an Array.

Nested schema metadata can also be applied to a single field by using "stacked" schema based annotations:

```java
interface MySetConfig extends Model {
    @SchemaAnnotations.Property(title = "My Set")
    @SchemaAnnotations.ArraySchema(defaultValue = "[\"a\"]")
    @SchemaAnnotations.StringSchema(defaultValue = "some-set-value", minLength = 1, maxLength = 1)
    Set<String> getMySet();
  }
```