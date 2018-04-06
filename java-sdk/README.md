# Lucidworks Connector Java SDK

## Overview
The Lucidworks Java Connector SDK provides a set of components designed to support building all types of Connectors.
Connectors can be created by implementing some of the classes described in this document and using the provided build tool.

Connectors can be deployed directly within a Fusion cluster or outside Fusion as a remote client.
To deploy a Connector into Fusion, you simply upload the build artifact (zip file) to the Fusion Blob API.
To deploy remotely, you can use our [Connector client utility](./plugin-client.md).

### Examples
The example Connectors are a great place to start understanding how to build a Java SDK Connector.
Have a look at the [README](connectors/README.md).

### Security
Read more on how to setup a secure environment for Java SDK Connectors [here](./security.md).

## Java SDK Framework Overview
There are only a few components which must be implemented in order to compile and build a functioning plugin.
Here, we'll give an overview of each of these primary components and related concepts. We'll start by describing a minimal
project directory layout, followed by details on the components.

### Project Layout
By default, we use Gradle for building Connectors. Other build tools can be used as well, but we'll stick with Gradle
in this document. Checkout the [Gradle quickstart](https://docs.gradle.org/current/userguide/tutorial_java_projects.html) for java Projects.

The minimal project layout is as follows:

    my-connector
    ├── build.gradle
    ├── gradle.properties
    ├── settings.gradle
    └── src
        └── main
            └── java
                └── Plugin.java
                └── Config.java
                └── Fetcher.java

#### build.gradle
This is a typical Gradle build file. There are a few important settings that must be present here. You can have a look
at an example [here](connectors/build.gradle).

#### gradle.properties
The build utility used for packaging up a Connector depends on this file and contains required metadata about the Connector,
including its name, version and the version of Fusion it can connect to.
Have a look at the example [here](connectors/random-connector/gradle.properties).

#### src/main/java
This is where Java code lives. This location can be changed, but we'll stick with the Gradle default.

### The Plugin
The `Plugin` class describes the various sub-components of a Connector, as well as its dependencies.
The current set of sub-components include the `Fetcher`, validation, security filtering, and configuration suggestions.

You can think of the `Plugin` class as the component that glues everything together for an implementation.

You can see an example
[here](connectors/random-connector/src/main/java/com/lucidworks/fusion/connector/plugin/RandomContentPlugin.java).
Notice that each sub-component can have a set of unique dependencies, or share dependencies across sub-components.

### The Configuration
The configuration component is an interface, which extends a base `Model` interface. By simply adding "getter" methods
and `@Property` annotations, a type-safe configuration object can be dynamically generated from a Connector's configuration data. Similarly, by implementing `Model`, a Fusion compatible JSON schema can also be generated.

You can see an example of a configuration implementation
[here](connectors/random-connector/src/main/java/com/lucidworks/fusion/connector/plugin/RandomContentConfig.java).

More detailed information on the configuration and schema capabilities can be found [here](configuration.md).

### The Fetcher
The `Fetcher` is where most of the work is done for any Connector.
This interface provides methods that define how data is fetched and indexed.

For a Connector to have its content indexed into Fusion, it must emit "messages", which are simple objects
that contain metadata and content. See the [message definitions](#message-definitions) below for more details.

#### Lifecycle

The `Fetcher` has a simple lifecycle for dealing with fetching content.
The flow outline is as follows:
```
    start()           - called once when the Job is started
        preFetch()    - called for each Phase
            fetch()   - called for each input to fetch
        postFetch()   - called for each Phase
    stop()            - called once when the Job is stopped
```

###### start()
When a `ConnectorJob` starts, the Fetcher's `start()` method is called. The main use case for a start() call
is to run setup code. This method is called once per job run.

The result of a start() call is metadata, which tells the connector controller service how to perform a crawl. Currently,
the main control feature enabled is to define fetch "phases". Each phase encapsulates a `preFetch() -> fetch() -> postFetch()`
call chain, and is defined by setting an ordered list of phase names in the start() response. This allows the Connector
to define a chain of distinct fetch blocks within a single job.

###### preFetch()
This method is called once per fetch phase and allows a Connector to emit data, that will eventually be
sent to the fetch() method. A common use case for this is to emit a large amount of metadata, which the controller service
later distributes across the cluster to perform the real fetch work.

###### fetch()
This is the primary fetch method for a crawler-like Connector (file system, web etc.). Messages emitted here
can include `Documents`, which will be indexed directly into the associated content Collection. There are several other
types of messages, see [below](#message-definitions).

###### postFetch()
Called after each phase for a given job run.

###### stop()
Called once per job run, at the end.

#### Phases
Connectors can define "phases", which are distinct blocks of fetch processing. This makes it possible to break up
fetching into steps, so that (for example), the first phase may fetch only metadata, while the second fetches content etc..

For each phase, the `preFetch`, `fetch` and `postFetch` methods are called. By default, every Connector has 1 implicit phase,
the "default" phase.

See the example [here](connectors/random-connector/src/main/java/com/lucidworks/fusion/connector/plugin/RandomContentFetcher.java).

<a name="message-definitions"></a>
#### Message Type Definitions

##### Candidate
A `Candidate` is metadata emitted from a `Fetcher` which represents a resource to _eventually_ fetch. Once this message is received by the controller service, it is persisted, then added to a fetch queue.
When this item is then dequeued, a Connector instance within the cluster will be selected,
and the message will be sent as a `FetchInput`. The `FetchInput` will be received in the `fetch()` method of the Connector.
At this point, the Connector will normally emit a [`Content`](#message-definitions.content) or [`Document`](#message-definitions.document) message, which is then indexed into Fusion.

The general flow of how Candidates are processed is the key to enabling distributed fetching within the Connectors framework.

##### FetchInput
The `FetchInput` represents an item to be fetched. Example values of `FetchInput`s are a file path, a URL, or a SQL statement. `FetchInputs` are passed to the fetch() method of a Fetcher and are derived from `Candidate` metadata.

<a name="message-definitions.document"></a>
##### Document
A `Document` is a value that is emitted from a Connector and represents structured content.
Once the controller service receives a `Document` message,
its metadata is persisted in the crawl-db and then sent along to the associated `IndexPipeline`.

<a name="message-definitions.content"></a>
##### Content
A `Content` message represents raw content that must be parsed in order to be indexed into Fusion. They are analagous to InputStreams, and their bytes are streamed to Fusion. `Content` types are actually composed of 3 different sub-types:
  * ContentStart - this tells Fusion that a stream of raw content is coming. It includes the `content-type` and any other metadata related to the source data.
  * ContentItem - this is a message that contains one smaller chunk of the raw content. These messages are streamed sequentially from a Connector to Fusion, and these are feed (without explicit buffering) directly to the index-pipeline.
  * ContentStop - this messages indicates to Fusion that the `Content` is done.
The end result of sending a `Content` stream, is a set of parsed documents within the Fusion Collection associated with the Connector.

##### Skip
`Skip` messages represent items that were not fetched for some reason.
For example, items that fail validation rules related to path depth or availability.
Each Skip can contains details on why the item was skipped.

##### Error
`Error` messages indicate errors for a specific item. For example, when a Connector's `fetch()` method is called
with an non-existant `FetchInput`, the Connector can emit an Error that captures the details ("not found" etc.).

Errors are recorded in the data store, but are not sent to the associated `IndexPipeline`.

##### Delete
`Deletes` tell the controller service to remove a specific item from the data store and associated Fusion Collection.

## Developing a Connector

The simplest way to get started developing a Connector is to have a look at the [examples](connectors/random-connector). We will eventually provide tools that enable a quicker startup experience.

### Dependencies
At a minimum, all plugins will require the Connector SDK dependency.
And more than likely, a handful of other dependencies as well.
Examples would be an HTTP client, a special client for connecting to a 3rd party REST API, or maybe a parser library that
can handle parsing video files.

Specifying these dependencies is done with the Java build tool of your choice (Gradle, Maven etc.).
But getting them into your code and even instantiating them is partly handled by the Connector SDK.

#### Dependency Injection
Allowing these plugins to specify runtime dependencies make them simpler to unit test, and generally more flexible.
When defining a Connector plugin, the object used for the definition also supports adding bindings for these dependencies.
A general guideline to follow when determining if something should be injected or not:

1. Is it desirable to have multiple implementations of the component?
2. Is the component a 3rd party library that has either difficult to control side-effects?

The Connectors uses the Guice framework for dependency injection: https://github.com/google/guice and can be directly used
when defining a Connector Plugin.

#### Maven
The Fusion Connectors SDK is currently not published to any public Maven repositories. We have plans to do this in the near future.