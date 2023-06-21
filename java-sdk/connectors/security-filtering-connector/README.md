# Security Filtering Connector

## Connector Description

The connector uses the connectors SDK to generate Access Control hierarchy and documents in order to demonstrate the 
Graph Security Trimming functionality.
The following steps are described below:
- How to build and deploy the security trimming connector.
- How to run the connector to generate Access Control documents and content documents.
- How to add the Graph Security Trimming stage to the Query Workbench and test security trimming.
- How to delete Access Control documents.

## Background
The Graph Security Trimming SDK methods generate the security hierarchy used by the Graph Security Trimming (GST)
stage of the query pipeline. In particular, for optimal efficiency, the GST stage supports only top level filtering queries -
cross collections joins are not supported. To facilitate that, Access Control documents are created in the content
document collection and a copy of each access control document is created on every active collection shard.

## The Access Control Hierarchy
The GST SDK methods facilitate the creation of a hierarchy of _principals_. A _principal_ is an entity that has
certain access permissions to resources. In the context of Fusion, the most common entities are _users_ and _groups_ and 
the typical access permission is the document _read_ permission. The GST SDK methods are abstract - they support
any _principal_ type and any permission. But this example demonstrates _users_, _groups_ and document _read_ permissions.

When the GST query pipeline stage is configured, documents are accessible only by principals who are specified
in the document's `_lw_acl_ss` list field. `user1` will be able to see the document only if it's specified 
in the document's `_lw_acl_ss` field, or if a group `user1` belongs to is specified in the `_lw_acl_ss` field. This access
authorization is transitive through group nesting.

When the sample connector populates a single shard collection, it creates 9 documents:  3 users, 3 groups 
and 3 content documents.
- _user1_
- _user2_
- _user3_
- _group1_ with _user1_ as a member
- _group2_ with _user1_ and _user2_ as members.
- _group3_ with _user3_ as a member
- _doc1_ with _group1_ in its `_lw_acl_ss` permissions list.
- _doc2_ with _group2_ in its `_lw_acl_ss` permissions list.
- _doc3_ with _group3_ in its `_lw_acl_ss` permissions list.

This hierarchy means that _user1_ has access to both documents and _user2_ has access only to _doc2_.


## Building and Deploying the Connector Plugin

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/security-filtering-connector
../gradlew assemblePlugin
```

2. This produces one zip file, named `security-filtering-connector.zip`, located in the `security-filtering-connector/build/libs` directory.
This artifact is now ready to be uploaded as a blob to Fusion as a connector plugin.

3. Use Fusion the admin `System -> Blobs` tab to upload and deploy the connector's zip file.
   Or see the following [instructions](../README.md) on how to build and deploy the plugin.


## Configuring and Running the Connector
After deploying the plugin zip, wait for `Security Demo Connector` to be available under the _Installed_ list of the `Indexing -> Datasources-> Add+` tab.
  It could take up to a minute until the plugin pod is ready.

Create a Security Demo Connector` datasource with this configuration:
- Enable security trimming under GRAPH SECURITY FILTERING CONFIGURATION
- set to _create_ the CRUD OPERATION option under CRAWL PROPERTIES.
- set to _NA_ (or any other value) the AC ID option under CRAWL PROPERTIES.
- Run the connector and wait for it to finish successfully. 
- Use the Query Workbench to observe the 9 created documents.

**Note that the format of documents id is: SOME-NAME___N document. N represents the shard#.
When the collection has a single shard, N will be 0 e.g. _user1___0_.**

##  Fields of Access Control Documents
Observe the following fields of groups and users:
- Since there are copies of each Access control object per shard, each copy must have a different doc-id. The values of *_lw_acl_ss* is
  the logical id which represents all the copies.
- The _inbound_ss_ value is the graph edge of the hierarchy - it points to the child nodes. Look, for example at the *_lw_acl_ss* value of
  _group2_. It contains _user1_, _user2_ and _group2_. The group points to its members and to itself in order to make trimming function work correctly.
- shard_s points to the shard id of this copy.
- The _outbound_ss_ field is not used explicitly in this example since we use the _inbound_ss_
  field to define the hierarchy. It is set automatically to point to itself.

## Testing the Query Security Trimming Stage
After the connector run, the documents are ready for testing GST at query time. Configure the GST stage at the 
 `Query Workbench -> Add a Stage` tab:

Add the Graph Security Trimming stage and configure as the following:
- Set _Join Field_ to _lw_acl_ss.
- Set _Join Method_ to topLevelDV.
- Set the _ACL Solr Collection_ to your content collection (by default, it is the same name as your Fusion app).

Test which documents _user2_ have access to:
- Add this parameter to the Query Workbench (in the upper right corner of the Query Workbench screen): `{username,user2}`
- Run the query _id:doc*_ and verify that only _doc2_ is retrieved since only _group1_ (which doesn’t include _user2_) is granted permission to see _doc1_.

Test which documents _user1_ have access to:
- Change the username parameter of the Query Workbench to _user1_.
- Run the query _id:doc*_ and verify that both _doc1_ and _doc2_ are retrieved because _doc2_ is permitted to _group2_ which 
includes _user1_ and _doc1_ is permitted to _group1_ which also includes _user1_.

Test which documents _user3_ have access to:
- Change the username parameter of the Query Workbench to _user3_.
- Run the query _id:doc*_ and verify that only _doc3_ is retrieved because _doc3_ is permitted only to _group3_ which
  includes only _user3_.

## Multiple Shard Collections
One way to test the GST sample connector with multiple shards is to split the collection before running the connector.
For example:

`https://some-proxy-host/solr/admin/collections?action=SPLITSHARD&collection=my_collection&shard=shard1`

After running the connector, observe that each shard got a copy of each user and each group.

## Deleting Control Access Documents
To test the deletion of Access Control documents configure the connector as the following:
- set to _delete_ the CRUD OPERATION option under CRAWL PROPERTIES.
- set to _group1_ the AC ID option under CRAWL PROPERTIES.
- Run the connector and observe that _group1_ is deleted.

Note that in the case of multiple shard collections, the SDK implementation deletes all the copies of the document across shards.

## SDK Code Observations
Here are some useful observations about the code of the security sample connector:
- The `SecurityConfig` interface must extend the `GraphSecurityConfig` interface.
- Observe how `context.newGraphAccessControl(...)`  in `SecurityFilteringAccessControlFetcher.java` is called to create users and groups.
- Observe how the `createDocuments(...)` method in `SecurityFilteringAccessControlFetcher.java` update _lw_acl_ss to determine which principals have access to the document.







