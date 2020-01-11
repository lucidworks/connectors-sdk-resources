# IMAP Connector

## Connector Description

Imap Connector fetches email messages from an IMAP server.

## Quick start

1. Clone the repo:
```bash
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/
./gradlew assemblePlugins

```

2. This will produce one zip file, named `imap-connector.zip`, located in the `build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a Connector plugin.

3. See the following [instructions](../README.md) on how to build, deploy, and run the plugin

## Connector properties

### Main properties
|Property Name| Property description|
|---|---|
| SSL | enable SSL connection|
| Folder | The folder to retrieve messages from |
| Host |  The hostname of the IMAP server |
| Username | Username to login |
| Password | The user's password |
