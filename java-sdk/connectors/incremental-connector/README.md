# Random Content Incremental Connector

This connector generates a configurable number of documents all with random titles and body fields. It has the following two properties:
- "Total": The total number of docs that will be generated in a first crawl
- "Incremental Total": The total number of docs to generate from the second and subsequent crawls"

This connector emit checkpoints and emit candidates as transient=true.

Crawls are incremental.

## Quick start

NOTE: This quick start assumes that Fusion is installed on the `/opt` path.

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/
./gradlew assemblePlugins
```

2. This produces one zip file, named `random-connector-incrementa.zip`, located in the `build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a connector plugin.

3. See the following [instructions](../README.md) on how to build, deploy, and run the plugin
