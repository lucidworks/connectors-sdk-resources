# Security filtering Connector

## Connector Description

The connector uses the connectors SDK to generate Access Control hierarchy and documents in order to demonstrate the 
Graph Security Trimming functionality.
The following steps are described below:
- How to build and deploy the security trimming connector.
- How to run the connector to generate Access Control documents and content documents.
- How to add the Graph Security Trimming stage to the Query Workbench to test security trimming.

## Background
The Graph Security Trimming SDK methods generate the security hierarchy used by the Graph Security Trimming (GST)
stage of the query pipeline. In particular, for optimal efficiency, the GST stage uses only top level filtering queries -
cross collections joins are not supported. To facilitate that, access control documents are created in the same collection where content
documents are created and a copy of each access control document is created on every active collection shard.

## The Access Control Hierarchy
The GST SDK methods facilitate the creation of a hierarchy of _principals_. A _principal_ is an entity that has
certain access permissions to resources. In the context of Fusion, the most common entities are _users_ and _groups_ and 
the typical access permission is the document _read_ permission. The GST SDK methods are abstract - they support
any _principal_ type and any permission. But this example demonstrate _users_, _groups_ and document _read_ permissions.

When security the GST query pipeline stage is configured, documents are accessible only by principals who are specified
in the document's `_lw_acl_ss` list field. `user1` will be able to see the document only if it's specified 
in the `_lw_acl_ss` field, or if a group `user1` belongs to is specified in the `_lw_acl_ss` field. This access
authorization is transitive through group nesting.

When the sample connector populates a single shard collections it creates 6 documents:  2 users, 2 groups 
and 2 content documents.
- _user1_
- _user2_
- _group1_ with _user1_ as a member
- _group2_ with _user1_ and _user2_ as members.
- _doc1_ with _group1_ in its `_lw_acl_ss` permissions list.
- _doc2_ with _group2_ in its `_lw_acl_ss` permissions list.

This hierarchy means that _user1_ has access to both documents and _user2_ has access only to _doc2_.

![Document structure](docs/png2.png)


## Building and Deploying the Connector Plugin

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/security-filtering-connector
../gradlew assemblePlugin
```

2. This produces one zip file, named `security-filtering-connector.zip`, located in the `security-filtering-connector/build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a connector plugin.

3. Use Fusion admin `System -> Blobs` tab to upload and deploy the connector's zip file.
   Or see the following [instructions](../README.md) on how to build and deploy the plugin.


## Configuring and Running the Connector
- After deploying the plugin zip, wait for `Security Demo Connector` to be under the _Installed_ list of the `Indexing -> Datasources-> Add+` tab.
  It could take about a minute.

Create a data source with this configuration:
- Enable security trimming under GRAPH SECURITY FILTERING CONFIGURATION
- set to _create_ the CRUD OPERATION option under CRAWL PROPERTIES.
- set to _NA_ the AC ID option under CRAWL PROPERTIES.
- Run the connector and wait for it to finish successfully. 
- Use the Query Workbench to observe the 6 created documents.

**Note the SOME-NAME___N document id naming convention of Access Control documents. N represents the shard#.
When the collection has a single shard, N will be 0 e.g. _user1___0_.**

##  Fields of Access Control Documents
Observe the following fields of groups and users:
- Since there are copies of each Access control object per shard, they each have different doc-id. The values of *_lw_acl_ss* is
  the logical id which represents all the copies as a single entity.
- The *inbound_ss* value is the hierarchy graph edge - it points to the children nodes in the hierarchy. Look, for example at the *_lw_acl_ss* value of
  _group2_. It contains _user1_, _user2_ and group2. As you can see, the group points to its members and to itself
  in order to make trimming function work correctly.
- shard_s points to the shard id of this copy.

## Security trimming stage (Query time)
After the connector run, the documents are ready for testing query security trimming. Configure the GST stage at the 
 `Query Workbench -> Add a Stage` tab:

Add the Graph Security Trimming stage and configure as the following:
- Set _Join Field_ to _lw_acl_ss.
- Set _Join Method_ to topLevelDV.

Test which documents _user2_ have access to:
- Add a parameter to the Query Workbench (in the upper right corner of the Query Workbench screen): {username,user2}
- Run the query _id:doc*_ and verify that only _doc2_ is retrieved since only group1 (which doesn’t include user2) is granted to see doc1.

Test which documents _user1_ have access to:
- Change the username parameter of the Query Workbench to _user1_.
- Run the query _id:doc*_ and verify that both _doc1_ and _doc2_ are retrieved because _doc2_ is permitted to _group2_ which 
includes _user1_ and _doc1_ is permitted to _group1_ which also includes user1.

## Multiple Shard Collections
One way to test the GST sample connector with multiple shards is to split the collection before running the connector.
For example:

`https://some-proxy-host/solr/admin/collections?action=SPLITSHARD&collection=my_collection&shard=shard1`

After running the connector, observe that each shard got a copy of each user each group.

## Deleting Control Access Documents
To test the deletion of an Access Control document configure the connector as the following:
- set to _delete_ the CRUD OPERATION option under CRAWL PROPERTIES.
- set to _group1_ the AC ID option under CRAWL PROPERTIES.
- Run the connector and observe that _group1_ is deleted.

Note that in the case of multiple shard collections, the SDK implementation deletes all the copies of the document across shards.

## SDK Code Observations
Here are some interesting observations about the code of the security sample connector:
- The *SecurityConfig* interface must extend the *GraphSecurityConfig* interface
- z
- y
- 






