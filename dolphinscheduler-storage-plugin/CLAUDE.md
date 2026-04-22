# CLAUDE.md — dolphinscheduler-storage-plugin

Plugin family for **resource storage** — where uploaded files, task resources, logs, and workflow artifacts live. Swappable between cloud blob stores and HDFS.

**This directory is a Maven parent POM.**

## Sub-modules

- **`dolphinscheduler-storage-api`** — SPI: `StorageOperator`, `StorageOperatorFactory`, `AbstractStorageOperator`, `StorageType`, `StorageConfiguration`.
- **`dolphinscheduler-storage-all`** — uber bundle for all implementations.
- Concrete plugins: `-s3` (AWS S3), `-hdfs` (Hadoop HDFS), `-oss` (Aliyun), `-gcs` (Google Cloud Storage), `-abs` (Azure Blob), `-obs` (Huawei), `-cos` (Tencent).

## SPI contract

`StorageOperator` (the core API):

- Path management: `getStorageBaseDirectory`, `mkdir`, `exists`, `delete`, `listStorageEntity`.
- I/O: `upload`, `download`, `fetchFileContent`.
- Tenancy: every method takes a `tenantCode`; multi-tenant isolation is baked in.

Each concrete plugin ships a `StorageOperatorFactory` annotated with `@AutoService(StorageOperatorFactory.class)`.

## Selection at runtime

Only **one** storage backend is active per cluster. `StorageConfiguration` reads `resource.storage.type` from config, iterates `ServiceLoader<StorageOperatorFactory>`, matches on `StorageType`, and produces the single `StorageOperator` bean.

Switching backends mid-life requires manual data migration — the system does not handle that.

## Gotchas

- **Tenant directory layout is part of the public contract**: `getStorageBaseDirectory(tenantCode)` determines where the UI, workers, and task plugins look for files. Changing the layout is a data-migration event.
- **`FileAlreadyExistsException`** semantics: `mkdir` on an existing dir throws, not no-ops. Callers must handle this — many do, but new call sites should too.
- **HDFS plugin pulls a very heavy Hadoop client tree**; exclusions in `-hdfs/pom.xml` are load-bearing. Watch out for transitive conflicts with `task-mr`, `task-spark`, `task-hivecli`.
- **S3 plugin is also exercised by the worker** for distributed-task artifact handling (not only as the resource store). This is the most battle-tested code path.
- **OBS listStorageEntity** had a bug where subdirectories were not returned — fixed recently (see commit `94bfbb048a`); if you see similar symptoms in a new plugin, compare against S3/OSS as reference impls.
- **Credentials**: cloud plugins use the SDK default chain when not explicitly configured. In AWS plugins, prefer IAM instance profile over static keys (see `dolphinscheduler-aws-authentication`).

## Tests

Per plugin in `src/test/java`, commonly with mocked SDK clients. A few use Testcontainers (`-s3` with LocalStack).

## Related modules

- `dolphinscheduler-aws-authentication` — AWS plugins' credential source.
- `dolphinscheduler-common` — utilities.
- `dolphinscheduler-worker`, `dolphinscheduler-api`, `dolphinscheduler-master` — runtime consumers.
