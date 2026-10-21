# Change-Only Crawler Connector

Test connector for CHECKPOINT recrawls with a fetcher that is not marked `INCREMENTAL`.

It reuses the fetcher and config of the [incremental connector](../incremental-connector/README.md): the first job emits `Total` documents, and each later job emits only the next `Incremental total` new documents. The difference is that the fetcher has no `FetcherType.INCREMENTAL` marker, so Fusion treats it as a CRAWLER and, in a CHECKPOINT run, stray-deletion purges every document the job did not emit again.

Plugin-Id `lucidworks.demo.change-only-crawler`, so it installs next to `lucidworks.demo.incremental`.

```
./gradlew :change-only-crawler-connector:assemblePlugin -Pversion=<version>
```
