# Public Connectors SDK Resources

This public Github repository provides resources to help developers build their own [Fusion](https://lucidworks.com/products/fusion-server/)
4 SDK Connectors.
Some of the resources include documentation and getting started guides, as well as example Connectors.

For developing a **Java SDK based Connector**, checkout the [README](./java-sdk/README.md).

Information on the current set of **example Java plugins** can be found [here](./java-sdk/connectors/README.md).

## Fusion 4 Connectors Overview

The new Connectors architecture is designed to be scalable. Depending on the Connector, jobs can now be scaled by adding new instances of just the Connector. The fetching process for these new types also supports distributed fetching, so that many instances can contribute to the same job.

Connectors can be hosted within Fusion, or can run remotely. In the hosted case, these Connectors are cluster aware. This means that when a new instance of Fusion starts up, the Connectors on other Fusion nodes become aware of the new Connectors, and vice versa. This makes scaling Connectors jobs simple.

In the remote case, Connectors become clients of Fusion. These clients run a very lightweight process and communicate to Fusion using an efficient messaging format. This option makes it possible to put the Connector wherever the data lives. In some cases, this might be required for performance or security/access reasons.

The communication of messages between Fusion and a remote Connector or hosted Connector are identical; Fusion sees them as the same kind of Connector. This means you can implement a Connector locally, connect to a remote Fusion for initial testing, and when done, upload the exact same artifact (a zip file) into Fusion, so Fusion can host it for you. The ability to run the Connector remotely makes the development process much quicker.

### Java SDK

The Java SDK brings a new set of components for making it simple to build a Connector in Java. Whether the plugin is a true crawler, or a simple iterative fetcher, the SDK supports both.

The [Java SDK](./java-sdk/README.md) includes a set of base classes and interfaces. We also provide the Gradle build utilities for packaging up a Connector, and a Connector client application that can run your Connector remotely.

Many of the base features needed for a Connector are provided by Fusion itself.
When a Connector first connects to Fusion, it sends along its name/type, schema and other metadata.
This connection then stays open, and the two systems are able communicate bi-directionally as needed.

This makes it possible for Fusion to manage configuration data, the job state, scheduling, and encryption for example. The Fusion Admin UI also takes care of the view/presentation, by making use of the Connector's schema.

This client-based approach decouples Connectors from Fusion which allows hot deployment of connectors through a simple connect call.

### Security
Fusion Connectors support SSL/TLS. See the security [README](security.md) for general setup.

### gRPC
The underlying client/server technology used by these Connectors, is a fast and efficient framework from Google called
[gRPC](https://grpc.io/).
gRPC provides flexibility in the way services and their methods are defined. Other benefits of this framework include:

* HTTP/2 based transport
* Provides a very efficient serialization format for data handling (protocol buffers)
* Allows bi-directional/multiplexed streams
* Flow-control a.k.a back-pressure

### Distributed Data Store
The data persisted by the new Connectors framework is distributed across the Fusion cluster. Each node holds its primary partition of the data, as well as backups of other partitions. If a node goes down during a crawl, the data store will continue to be intact and usable. Connector implementations don't need to be concerned with this layer, it's all handled by Fusion.


Copyright 2018 [Lucidworks](https://lucidworks.com)
