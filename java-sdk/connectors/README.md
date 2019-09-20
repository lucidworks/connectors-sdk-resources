This a Gradle project that wraps each [known plugin](settings.gradle) with a common set of tasks
and dependencies.

See the following instructions on how to build, deploy, and run the plugins

## Building

**Important**


As a first step, change the _fusionHome_ property in the [`gradle.properties` file](gradle.properties)

The _fusionHome_ property is the full path of your local Fusion installation. The path should include Fusion's version.

For example `/opt/fusion/4.2.4`

The _fusionHome_ is needed to import the code to an IDE.

### Building the Plugin Zip

From `java-sdk/connectors`, execute
```bash
./gradlew clean build :assemblePlugins
```

This produces the zip files, e.g. `random-connector.zip` located in the `build/libs` directory.

At this point, the generated zip could be uploaded directly to Fusion, but follow the steps below to run as a remote plugin.


## Start

Connect to Fusion plugin by using the client jar plus the plugin zip:

```bash
java  -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/random-connector.zip
+```

Start with debugging enabled:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010 -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/random-connector.zip
```

Alternatively, when proper configuration is done in the `gradle.properties` file (fusionHome, fusionVersion and fusionRpcTarget properties), the plugin can also be run using a simple gradle task:

```bash
./gradlew connect
```

After running this, logging should show that it either was able to connect to Fusion, or not. If not, be sure you're using the right Fusion address/port. If not, the client jar can accept various related settings.

## Fusion
After the client process successfully connects to Fusion, you should see connector available in Fusion as a new connector type.
