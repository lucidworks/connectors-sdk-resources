# Fusion Connector Plugin Client

This project provides a wrapper for our Fusion Java plugin-sdk so that our plugins don't need to directly talk with gRPC code. Instead, they can use high level interfaces and base classes, like Connector and Fetcher etc.

This project also provides a standalone "runner" that can host a plugin that was built from the Fusion Java Connector SDK. It does this by loading the plugin zip file, then calling on the wrapper to provide the gRPC interactions. 

## Java SDK / gRPC Wrapper

One of the primary goals of the plugin-client is to isolate plugin code from the underlying framework details.
Specifically, the underlying message formats (protocol buffers) and native gRPC code.
This makes it possible to make some changes to the base support
layer, without having to make changes to the Java plugin implementation.

## Standalone Connector Plugin Application

The second goal of the plugin-client, is to allow Java SDK plugins to run remotely.
The instructions for deploying a Connector using this method are provided below.

### Building the UberJar

To build the uber-jar, run:

```
./gradlew :fusion-connectors:grpc:connector-plugin-client:shadowJar
```

The uberjar is built and placed in this location:

```
fusion-connectors/grpc/connector-plugin-client/build/libs/connector-plugin-client-4.0.0-SNAPSHOT-uberjar.jar
```
### Starting the Host

To start the host app, a Fusion-SDK based Connector, built into the standard packaging format as a `.zip` file is required. This `zip` must contain only 1 Connector plugin.

Here's an example of how to start up using the web connector:

```
java -jar fusion-connectors/grpc/connector-plugin-client/build/libs/connector-plugin-client-4.0.0-SNAPSHOT-uberjar.jar fusion-connectors/build/plugins/connector-web-4.0.0-SNAPSHOT.zip
```

To run the client with remote debugging enabled:

```
java -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=5010 -jar fusion-connectors/grpc/connector-plugin-client/build/libs/connector-plugin-client-4.0.0-SNAPSHOT-uberjar.jar fusion-connectors/build/plugins/connector-web-4.0.0-SNAPSHOT.zip
```