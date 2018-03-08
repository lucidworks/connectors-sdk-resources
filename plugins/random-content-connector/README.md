# Random Content Connector

This Connector generates a configurable number of Documents, all with random titles and body fields.

## Building

**Important**

Change the _fusionHome_ property in the [Gradle Properties File](gradle.properties) 

The _fusionHome_ property is the full path of your local Fusion installation. The path should include Fusion's version.

For example `/opt/fusion/4.0.1`

### Random Content Plugin

Build the plugin zip:

```bash
./gradlew clean assemblePlugin
```

This will produce one zip file, named `random-content-connector-{version}.zip`, located in the `build/plugins` directory.

At this point, the generated zip could be uploaded directly to Fusion, but follow the steps below to run as a remote plugin.



## Start

Connect to Fusion plugin by using the client jar + the plugin zip:

```bash
java  -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/random-content-connector-{version}.zip
```

Start with debugging enabled:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010 -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/random-content-connector-{version}.zip
```

After running this, logging should show that it either was able to connect to Fusion, or not. If not, be sure you're using the right Fusion address/port. If not, the client jar can accept various related settings.

## Fusion
After the client process successfully connects to Fusion, you should see the Random Content Connector available in Fusion as a new Connector type.

