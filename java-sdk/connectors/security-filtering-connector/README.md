# Security filtering Connector

## Connector Description

The connector uses the connectors SDK to generate Access Control hierarchy and documents in order to demonstrate the 
Graph Security Trimming functionality.
The following steps are described below:
- How to build and deploy the security trimming connector.
- How to run the connector to generate Access Control documents and content documents.
- How to add the Graph Security Trimming stage to the Query Workbench to test security trimming.

## Background
The Graph Security Trimming SDK methods generate the security hierarchy used by the Graph Security Trimming (SGT)
stage of the query pipeline. In particular, for optimal efficiency, the SGT stage uses only top level filtering queries -
cross collections joins are not supported. To facilitate that, access control documents are created in the same collection where content
documents are created and a copy of each access control document is created on every active collection shard.

## The Access Control Hierarchy
The SGT SDK methods facilitate the creation of a hierarchy of _principals_. A _principal_ is an entity that has
certain access permissions to resources. In the context of Fusion, the most common entities are _users_ and _groups_ and 
the typical access permission is the document _read_ permission. The SGT SDK methods are abstract - they support
any _principal_ type and any permission. But this example demonstrate _users_, _groups_ and document _read_ permissions.

When security the SGT query pipeline stage is configured, documents are accessible only by principals who are specified
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


## Connector properties

### Main properties

### Security trimming properties

|Property Name| Property description|
|---|---|
| Enable security trimming | When enable, the access control fetcher will be executed |
| ACL collection name | The name of the collection where the documents indexed by access control fetcher will be indexed |

### Content generator properties

|Property Name| Property description|
|---|---|
| Total | The total number of documents to generate |
| Minimum number of sentences | The minimum number of sentences to generate per document, the random generator will use this value as lower bound to calculate a random number of sentences|
| maximum number of sentences | The maximum number of sentences to generate per document, the random generator will use this value as upper bound to calculate a random number of sentences|

## Code Observations

- The job run uses two phases: The first phase is used by SecurityFilteringContentFetcher to create documents in the content collection
  and create candidates for the security documents (permissions users and groups). The second phase is used by 
  SecurityFilteringAccessControlFetcher to create security documents. The phases are created in SecurityFilteringPlugin with
  this code that creates the CONTENT phase for SecurityFilteringContentFetcher and the ACCESS_CONTROL phase 
```java
   return ConnectorPlugin.builder(SecurityFilteringConfig.class)
    .withFetcher(CONTENT, SecurityFilteringContentFetcher.class, fetchModule)
    .withFetcher(ACCESS_CONTROL, SecurityFilteringAccessControlFetcher.class, fetchModule)
```
SecurityFilteringContentFetcher sends the document to the second phase with:
```java
      fetchContext.newCandidate(permission.getId())
       .withTargetPhase(ACCESS_CONTROL)
```
- Observe how user memberships in a group is created by setting an outbound field in the user document that 
points to the group(s):
```java
    outbound = Collections.singletonList(parentGroup);
    ...
    context.newAccessControl(input.getId())
    ...
    .addAllOutbound(outbound)

```
- Observe how principals (users and groups) are set in the permission document using the inbound field:
```java
   context.newAccessControl(input.getId())
    ...
    .addAllInbound(Collections.singletonList(assigned))

```
- Observe how permissions are correlated to the logged-in user at search time through the withPrincipal method
in SecurityFilteringPlugin:
```java
  .withSecuritySpec(sf -> sf
        .staticSpec(spec -> spec
        .withPrincipal("fullName_s")))

```
## ACL Collection Observations
After running the connector, you can observe the ACL collection documents generated by SecurityFilteringAccessControlFetcher. Use the Collection Manager
to find the ACL collection. Its name will be `{DATASOURCE_ID}_access_control_hierarchy` if you kept the default name. 
Use the Query Workbench
to list the documents and see how the field values correspond to the code that explained above.

- Observe the permission, user and group documents.
- Show all the document's fields and see how inbound permission fields point to users and groups.
- Observe how the outbound field of users with group memeberships point to the group(s).
- Observe the fullName_s field of users and groups that correlate it to the logged-in principal at search time.

## Security trimming stage (Query time)

After indexing documents in the content and access control collections, we can run a search query to filter the results based on a username.
First, we need to add the `Security Trimming` query stage to the query pipeline and configure it.

Check the access control collection to review which users and groups were indexed.
