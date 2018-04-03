# Example Connectors

## Quick start

Tl;dr
* The quick start assumes that Fusion is installed on the `/opt` path and its version is `4.0.1` 

1. Clone the repo:
```bash
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/plugins/java/connectors/
```

2. Build all plugins
```bash
./gradlew assemblePlugins -PfusionHome=/opt/fusion/4.0.1 -PfusionVersion=4.0.1 
```

3. This will produce one zip file per plugin, located in the `build/plugins` directory, that can be uploaded directly to Fusion.

## Building

**Important**

Change the _fusionHome_ property in the [Gradle Properties File](gradle.properties) 

The _fusionHome_ property is the full path of your local Fusion installation. The path should include Fusion's version.

For example `/opt/fusion/4.0.1`

The _fusionHome_ is needed to import the code to an IDE.