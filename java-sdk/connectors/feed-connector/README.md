# Feed Connector

'Feed Connector' fetches the entries of a feed file. The file must to be in json format and has [this format](https://gist.github.com/mcondo/1186e69d8bbf2779c4db30d43a52850f). The connector has one property:
- "Feed file path": Feed file path location

This connector emit checkpoints, emit candidates as transient=true, and enable the 'Purge Stray Items' feature.

Crawls are incremental.

### How to use 'Feed Connector'
- Start a crawl. Let it finishes. All the entries are indexed.
- Add/modify/remove entries from the feed file. Start another crawl. The changes are reflected in the content collection:
    - Removed entries from feed file: those will be removed automatically from the content collection (because of 'Purge Stray Items')
    - New/modified entries from the feed file: before starting the crawl, you need to update manually the 'lastUpdated' value of those entries. That value represents the time (in ms) when the entry is modified/added and must be greater than the last time you run a crawl (i.e. your current time)

## Quick start

NOTE: This quick start assumes that Fusion is installed on the `/opt` path.

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/
./gradlew assemblePlugins
```

2. This produces one zip file, named `feed-connector-{version}.zip`, located in the `build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a connector plugin.

## Building

**Important**


As a first step, change the _fusionHome_ property in the https://github.com/lucidworks/connectors-sdk-resources/blob/master/java-sdk/connectors/gradle.properties[`gradle.properties` file^].

The _fusionHome_ property is the full path of your local Fusion installation. The path should include Fusion's version.

For example `/opt/fusion/4.2.4`

The _fusionHome_ is needed to import the code to an IDE.

### Feed Plugin

Build the plugin zip:

```bash
./gradlew clean build :assemblePlugin
```

This produces one zip file, named `feed-connector.zip`, located in the `build/libs` directory.

At this point, the generated zip could be uploaded directly to Fusion, but follow the steps below to run as a remote plugin.


## Start

Connect to Fusion plugin by using the client jar plus the plugin zip:

```bash
java  -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/feed-connector-{version}.zip
```

Start with debugging enabled:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010 -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/feed-connector-{version}.zip
```

Alternatively, when proper configuration is done in the `gradle.properties` file (fusionHome, fusionVersion and fusionRpcTarget properties), the plugin can also be run using a simple gradle task:

```bash
./gradlew connect
```

After running this, logging should show that it either was able to connect to Fusion, or not. If not, be sure you're using the right Fusion address/port. If not, the client jar can accept various related settings.

## Fusion
After the client process successfully connects to Fusion, you should see the Feed Connector available in Fusion as a new connector type.