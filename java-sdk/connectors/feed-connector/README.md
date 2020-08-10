# Feed Connector

## Connector Description

Feed Connector fetches the entries of a feed file. The file must to be a json file with [this format](../../resources/feed-simple-small.json). If no file is specified, then the connector will generate random contents, following the configurations in the `Generated properties` section.

This connector emits checkpoints, emits candidates as transient=false, and enables the 'Purge Stray Items' feature.

Crawls are incremental.

## Quick start

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/
./gradlew assemblePlugins
```
2. This produces the zip file, named `feed-connector.zip`, located in the `build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a connector plugin.

3. See the following [instructions](../README.md) on how to build, deploy, and run the plugin

## Connector properties

### Main properties
|Property Name| Property description|
|---|---|
|Feed file path | the path to the file containing entries in the [format](../../resources/feed-simple-small.json) |

### Generate Properties

The following properties will be taken if no `Feed file path` was provided.

|Property Name| Property description|
|---|---|
| Total to generate first crawl | the number of documents to generate for the first crawl |
| Total incremental to add | the number of documents to generate in the second and following jobs |
| Total incremental to remove | the number of documents to delete in the second and following jobs |

## How to use the connector

#### Using a local json file
- Set 'Feed file path' property
- Start a crawl. Let it finishes. All the entries are indexed.
- Add/modify/remove entries from the feed file. Start another crawl. The changes are reflected in the content collection:
    - Removed entries from feed file: those will be removed automatically from the content collection (because of 'Purge Stray Items')
    - New/modified entries from the feed file: before starting the crawl, you need to update manually the 'lastUpdated' value of those entries. That value represents the time (in ms) when the entry is modified/added and must be greater than the last time you run a crawl (i.e. your current time)

#### Using the "Generate properties"

- Not provide a json file.
- 'Generate Properties' has default values. From UI, see 'GENERATE PROPERTIES'
- Start a crawl. Let it finishes. By default, 1000 items will be indexed. Item ids are from 0 to 999
- Start another crawl. Let it finishes. total 905 items exist in content collection:  Item ids are from 100 to 1004
   - The first 100 entries (0-99) will be removed
   - 5 new items are added (1000-1004)
- Start another crawl. Let it finishes. total 810 items exist in content collection: Item ids are from 200 to 1009
   - 100 more entries will be removed (100-199)
   - 5 more new items are added (1005-1009)  
- Etc
 

Note: If after some subsequent crawls, all the documents are removed:
 - clear the datasource (checkpoint wil be removed)
 - change some properties if desired and save the datasource. 
 - Start crawling again

## Upgrading a feed based connector to SDK version 2.0.1 

The SDK version 2.0.1 brings changes to the tagging and removing of stray items. These are areas to update when upgrading your feed connector to SDK 2.0.1 or above.
1. PostFetchResult - It is no longer necessary to override this method to perform the purging of stray items. If the PostFetch method uses `withPurgeStrayItems()`, it can be removed.
2. When emitting candidates `fetchContext.newCandidate`, remove `withTransient(true)` or set the property to false `withTransient(false)`.