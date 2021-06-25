# Connectors

This is a Gradle project that wraps each [known plugin](settings.gradle) with a common set of tasks
and dependencies.

See the following instructions on how to build, deploy, and run the plugins.

## Sub-projects

This repository contains connector implementations(see the list [here](settings.gradle) ) and a [shared library](shared-lib).

The project structure of each connector is defined [here](../README.asciidoc#project-layout).

The `shared-lib` sub-project contains common classes and utility methods used on different connector implementations.
To include the shared library in a connector project, add it as a dependency in the build file:

```
dependencies {
  compile project(":shared-lib")
}
```
 For example, the simple-connector [build](simple-connector/build.gradle) project is including the `shared-lib` project.

## Building the Plugin Zip file

From `java-sdk/connectors`, execute the assemblePlugins task
```bash
./gradlew clean assemblePlugins
```

This produces the zip files, e.g. `simple-connector.zip` located in the `build/libs` directory.

At this point, the generated zip could be uploaded directly to Fusion, but follow the steps below to run as a remote plugin.

To target a specific SDK version, checkout the appropriate git tag version before bulding the plugin zip. 

## Deploy to Fusion

From `java-sdk/connectors/simple-connector`, execute the deploy task to automatically deploy the connector to a Fusion, the url can be configurated as show. By default points to localhost.
```bash
./gradlew deploy -PrestService=http://127.0.0.1:6764/connectors
```

While developing the plugin, you will need a way to deploy your changes quick and easy to Fusion.

Here is a Linux/Mac example of how to deploy your SDK connector to Fusion which uses `curl`:

```bash
FUSION_PROXY_URL="http://127.0.0.1:6764/connectors/plugins"
PLUGIN_ZIP_PATH="./feed-connector/build/libs/feed-connector.zip"
FUSION_USERNAME="admin"
FUSION_PASSWORD="password"
curl -u "${FUSION_USERNAME}:${FUSION_PASSWORD}" -X PUT -H "content-type:application/zip" "${FUSION_PROXY_URL}" --data-binary "@${PLUGIN_ZIP_PATH}"
```

Here is a Windows example of how to deploy your SDK connector to Fusion which uses `powershell`:

```powershell
$fusion_proxy_url="http://127.0.0.1:6764/connectors/plugins"
$plugin_zip_path="./feed-connector/build/libs/feed-connector.zip"
$fusion_username = "admin"
$fusion_password = "password"
$base64AuthInfo = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes(("{0}:{1}" -f $fusion_username,$fusion_password)))
Invoke-RestMethod -Headers @{Authorization=("Basic {0}" -f $base64AuthInfo)} -Method PUT -ContentType "application/zip" "${FUSION_PROXY_URL}" -InFile "${PLUGIN_ZIP_PATH}"
```


## Upgrade Connector implementation to Fusion 5

In order to upgrade to Fusion 5, the sdk dependency to be upgraded to `connector-plugin-sdk:2.0.0` version. The way to implement the plugin definition class has changed.
Below is an example on how to implement the plugin definition class:


```
import com.google.inject.AbstractModule;
import com.google.inject.Module;

import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin;
import com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider;
import com.lucidworks.connector.plugins.security.config.SecurityFilteringConfig;

public class MyPlugin implements ConnectorPluginProvider { // <1>

  @Override
  public ConnectorPlugin get() { // <2>
    Module fetchModule = new AbstractModule() {
      @Override
      protected void configure() {
        bind(MyClient.class).to(MyHttpClientImpl.class).asEagerSingleton();  // <3>
      }
    };

    return ConnectorPlugin.builder(MyPluginConfig.class) // <4>
        .withFetcher("content", MyContentFetcher.class, fetchModule) // <5>
        .withFetcher("access_control", MyAccessControlFetcher.class, fetchModule) // <6>
        .withSecurityFilter(MySecurityFilterComponennt.class, fetchModule)
        .withValidator(MyValidator.class, fetchModule)
        .build(); // <7>
  }
}
```
1. The plugin definition class should implement `com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPluginProvider` interface.
2. Implement the `get()` method(it should return a `com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin` object).
3. Define the bindings in this method implementation, as usual.
4. Use the `com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin.build()` static method to get an instance of the `ConnectorPlugin.Builder` class. Pass the connector configuration class as parameter(the class where the connector properties are defined).
5. Add the fetcher(s) classes to the build by using `ConnectorPlugin.Builder.withFetcher()` method
6. Set other components to the plugin definition. See the `com.lucidworks.fusion.connector.plugin.api.plugin.ConnectorPlugin.Builder` class for more details.
7. Call the `build()` method on the build instance.

See more examples of the plugin definition classes in the [example connectors](https://github.com/lucidworks/connectors-sdk-resources/tree/v2.0.3/java-sdk/connectors/README.md) project.
