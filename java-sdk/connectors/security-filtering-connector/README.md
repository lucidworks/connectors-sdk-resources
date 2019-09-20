# Security Filtering Sample Connector

- This connector generates a configurable number of documents, all with random titles and body fields.
- Also document ACLs, groups and users will be generated at demand.

## Quick start

1. Clone the repo:
```
git clone https://github.com/lucidworks/connectors-sdk-resources.git
cd connectors-sdk-resources/java-sdk/connectors/
./gradlew assemblePlugins
```

2. This produces one zip file, named `security-filtering-connector.zip`, located in the `security-filtering-connector/build/plugins` directory.
This artifact is now ready to be uploaded directly to Fusion as a connector plugin.

3. See the following [instructions](../README.md) on how to build, deploy, and run the plugin

## Security Filtering Plugin Properties

| Property name | Description |
| ------------- | ----------- |
| totalNumDocs | Total number of docs to generate |
| numberOfNestedGroups | Number of nested groups |

### Access Control Fetcher behavior

- This fetcher was implemented in SecurityFilteringAccessControlFetcher class
- `numberOfNestedGroups` represents the number of nested groups levels, for each level, the same number of groups will be created, and a user who will own them will be created too.
    - Group ID format `group_<level>_<group_count>`
    - User ID format `user_<level>`
    - Groups in a higher level will be parent group of the next groups level
    - Groups level threshold will be `1 <= X <= numberOfNestedGroups`
- For each document, a document ACL will be emitted, document ACLs will randomly own groups as part of their inbound field

## Security trimming stage

- There are not explicit changes in the stage, add the Security trimming stage to the properly pipeline, 
and use any user (`user_Y` where `1 <= Y <= numberOfNestedGroups`) as username value to create the security filter.