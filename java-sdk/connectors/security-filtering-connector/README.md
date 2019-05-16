# Security Filtering Sample Connector

- This connector generates a configurable number of documents, all with random titles and body fields.
- Also document ACLs, groups and users will be generated at demand.

## Quick start

NOTE: This quick start assumes that Fusion is installed on the `/opt` path and its version is `4.2.0`

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/
./gradlew assemblePlugins -PfusionHome=/opt/fusion/4.2.0 -PfusionVersion=4.2.0
```

2. This produces one zip file, named `security-filtering-connector-{version}.zip`, located in the `security-filtering-connector/build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a connector plugin.

## Building

**Important**


As a first step, change the _fusionHome_ property in the https://github.com/lucidworks/connectors-sdk-resources/blob/master/java-sdk/connectors/gradle.properties[`gradle.properties` file^].

The _fusionHome_ property is the full path of your local Fusion installation. The path should include Fusion's version.

For example `/opt/fusion/4.2.0`

The _fusionHome_ is needed to import the code to an IDE.

### Security Filtering Plugin

Build the plugin zip under `security-filtering-connector` directory:

```bash
../gradlew clean build :assemblePlugin
```

This produces one zip file, named `rsecurity-filtering-connector.zip`, located in the `build/libs` directory.

At this point, the generated zip could be uploaded directly to Fusion, but follow the steps below to run as a remote plugin.


## Start

Connect to Fusion plugin by using the client jar plus the plugin zip:

```bash
java  -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/security-filtering-connector-{version}.zip
```

Start with debugging enabled:

```bash
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010 -jar ${fusionHome}/apps/connectors/connectors-rpc/client/connector-plugin-client-${fusionVersion}-uberjar.jar build/plugins/security-filtering-connector-{version}.zip
```

Alternatively, when proper configuration is done in the `gradle.properties` file (fusionHome, fusionVersion and fusionRpcTarget properties), the plugin can also be run using a simple gradle task:

```bash
./gradlew connect
```

After running this, logging should show that it either was able to connect to Fusion, or not. If not, be sure you're using the right Fusion address/port. If not, the client jar can accept various related settings.

## Fusion
After the client process successfully connects to Fusion, you should see the Security Filtering Sample Connector available in Fusion as a new connector type.

## Security Filtering Plugin Properties

| Property name | Description |
| ------------- | ----------- |
| totalNumDocs | Total number of docs to generate |
| numberOfNestedGroups | Number of nested groups |

### Access Control Fetcher behavior

- This fetcher was implemented in SecurityFilteringAccessControlFetcher class
- `numberOfNestedGroups` represents the number of nested groups levels, for each level, the same number of groups will be created, and a user who will own them will be created too.
    - Group ID format `group_<level>_<group_count>`
    - User ID format `user_<level>`
    - Groups in a higher level will be parent group of the next groups level
    - Groups level threshold will be `1 <= X <= numberOfNestedGroups`
- For each document, a document ACL will be emitted, document ACLs will randomly own groups as part of their inbound field

## Security trimming stage

- There are not explicit changes in the stage, add the Security trimming stage to the properly pipeline, 
and use any user (`user_Y` where `1 <= Y <= numberOfNestedGroups`) as username value to create the security filter.