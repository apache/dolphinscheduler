# AGENT.md - Apache DolphinScheduler

Apache DolphinScheduler is a distributed, visual DAG workflow-scheduling platform. This is the monorepo: backend servers (master / worker / api / alert), a Vue 3 frontend, plugin families for tasks / datasources / storage / alerting / scheduling, and the release tooling.

**This file is an agent-facing project index, adapted from `CLAUDE.md`.** Module-specific details currently live in each module's `CLAUDE.md`; use those files as the source of truth and do not duplicate module contents here.

---

## Tech stack (project-wide)

- **Java 1.8** (do not assume 11+ APIs; `dolphinscheduler-api-test` is the only Java 11 island).
- **Spring Boot 2.6.1** across servers, **Jetty** (Tomcat is excluded transitively).
- **MyBatis-Plus** for ORM; **HikariCP** for the metadata DB pool, **Druid** inside user-facing datasource plugins.
- **Quartz** for cron scheduling (via `scheduler-plugin`).
- **Netty / gRPC** for inter-server RPC (see `extract-base`).
- **Vue 3 + Vite + TypeScript + Naive UI** for the frontend.
- **Maven** multi-module reactor (26 modules in root `pom.xml` + 2 test modules).
- **Zookeeper 3.8** by default for the registry (Etcd and JDBC also supported).

## Runnable services

A production deployment runs **four independent services** (plus an external registry and metadata DB). A fifth entry point, `StandaloneServer`, embeds all four in one JVM for development.

| Service | Module | Main class | Default ports |
|---------|--------|------------|---------------|
| **API** | [`dolphinscheduler-api`](dolphinscheduler-api/CLAUDE.md) | `org.apache.dolphinscheduler.api.ApiApplicationServer` | `12345` (HTTP / UI + REST) |
| **Master** | [`dolphinscheduler-master`](dolphinscheduler-master/CLAUDE.md) | `org.apache.dolphinscheduler.server.master.MasterServer` | `5679` (RPC) |
| **Worker** | [`dolphinscheduler-worker`](dolphinscheduler-worker/CLAUDE.md) | `org.apache.dolphinscheduler.server.worker.WorkerServer` | `1235` (RPC) |
| **Alert** | [`dolphinscheduler-alert`](dolphinscheduler-alert/CLAUDE.md) (to `-alert-server`) | `org.apache.dolphinscheduler.alert.AlertServer` | `50053` (HTTP), `50052` (RPC) |
| Standalone (dev only) | [`dolphinscheduler-standalone-server`](dolphinscheduler-standalone-server/CLAUDE.md) | `org.apache.dolphinscheduler.StandaloneServer` | `12345` + `50052` (API + alert; master/worker use in-JVM calls) |

Every service is a `@SpringBootApplication` on Jetty and implements `IStoppable`. Scale Master / Worker / Alert horizontally; coordination happens via the registry (Zookeeper by default). API is stateless and also scales horizontally behind a load balancer.

Ports are overridable via `server.port` / service-specific keys in each service's `application.yaml`.

## Build & run

```bash
# Full build (release profile; produces dist tarball)
./mvnw clean install -Prelease

# Zookeeper 3.4 legacy
./mvnw clean install -Prelease -Dzk-3.4

# Skip UI build (faster iteration on backend only)
./mvnw -pl '!dolphinscheduler-ui' clean install

# Build one module (+ its required siblings)
./mvnw -pl dolphinscheduler-master -am clean install

# Format (Spotless is configured)
./mvnw spotless:apply

# Standalone server (after building)
cd dolphinscheduler-standalone-server/target && ./bin/start.sh
```

Binary artifact: `dolphinscheduler-dist/target/apache-dolphinscheduler-*-bin.tar.gz`.

## Test

```bash
# Unit tests for one module
./mvnw -pl dolphinscheduler-master test

# API integration tests (separate reactor, requires Docker)
mvn -pl dolphinscheduler-api-test/dolphinscheduler-api-test-case test

# E2E browser tests (Selenium + Docker)
mvn -pl dolphinscheduler-e2e/dolphinscheduler-e2e-case test

# Apple Silicon: add -Dm1_chip=true to the Docker-driven suites
```

---

## Module index

Click into a module's `CLAUDE.md` for details. Each description is one line here on purpose.

### Core execution

- [`dolphinscheduler-master`](dolphinscheduler-master/CLAUDE.md) - workflow orchestration engine; consumes `Command`s, runs the DAG state machine, dispatches to workers.
- [`dolphinscheduler-worker`](dolphinscheduler-worker/CLAUDE.md) - runs physical tasks dispatched from master; hosts task plugins.
- [`dolphinscheduler-task-executor`](dolphinscheduler-task-executor/CLAUDE.md) - reusable task-lifecycle framework embedded by the worker.
- [`dolphinscheduler-alert`](dolphinscheduler-alert/CLAUDE.md) - alert server + channel plugins (email, Feishu, DingTalk, ...).

### API layer

- [`dolphinscheduler-api`](dolphinscheduler-api/CLAUDE.md) - REST API server (entry point for UI, Python SDK, external clients).
- [`dolphinscheduler-api-test`](dolphinscheduler-api-test/CLAUDE.md) - integration tests against the REST API (Docker Compose + Testcontainers).
- [`dolphinscheduler-authentication`](dolphinscheduler-authentication/CLAUDE.md) - Actuator-endpoint auth + AWS credential helpers (NOT the main login path).

### Shared libraries

- [`dolphinscheduler-common`](dolphinscheduler-common/CLAUDE.md) - foundation utilities (everything depends on this).
- [`dolphinscheduler-dao`](dolphinscheduler-dao/CLAUDE.md) - MyBatis DAO layer + SQL migration scripts.
- [`dolphinscheduler-service`](dolphinscheduler-service/CLAUDE.md) - business logic between DAO and the servers.
- [`dolphinscheduler-spi`](dolphinscheduler-spi/CLAUDE.md) - Service-Provider Interface root (every plugin depends on this).
- [`dolphinscheduler-extract`](dolphinscheduler-extract/CLAUDE.md) - RPC interface contracts between servers.
- [`dolphinscheduler-eventbus`](dolphinscheduler-eventbus/CLAUDE.md) - in-process event-bus abstractions.
- [`dolphinscheduler-registry`](dolphinscheduler-registry/CLAUDE.md) - pluggable registry (Zookeeper / Etcd / JDBC).
- [`dolphinscheduler-meter`](dolphinscheduler-meter/CLAUDE.md) - metrics (Prometheus) + server load-protection primitives.

### Plugin families

- [`dolphinscheduler-task-plugin`](dolphinscheduler-task-plugin/CLAUDE.md) - task-type plugins (shell, SQL, Spark, Flink, K8s, EMR, ...). 33 concrete plugins.
- [`dolphinscheduler-datasource-plugin`](dolphinscheduler-datasource-plugin/CLAUDE.md) - user-facing datasource plugins (MySQL, Hive, Trino, Snowflake, ...). 28 concrete plugins.
- [`dolphinscheduler-storage-plugin`](dolphinscheduler-storage-plugin/CLAUDE.md) - resource storage (S3, HDFS, OSS, GCS, ABS, OBS, COS).
- [`dolphinscheduler-scheduler-plugin`](dolphinscheduler-scheduler-plugin/CLAUDE.md) - cron scheduler (Quartz today).
- [`dolphinscheduler-dao-plugin`](dolphinscheduler-dao-plugin/CLAUDE.md) - metadata-DB dialect support (MySQL / PostgreSQL / H2).

### Build, ops, tools

- [`dolphinscheduler-bom`](dolphinscheduler-bom/CLAUDE.md) - Maven BOM; central dependency version pinning.
- [`dolphinscheduler-dist`](dolphinscheduler-dist/CLAUDE.md) - assembles the release tarball + Docker images.
- [`dolphinscheduler-standalone-server`](dolphinscheduler-standalone-server/CLAUDE.md) - all-in-one JVM with H2 (dev / smoke tests).
- [`dolphinscheduler-tools`](dolphinscheduler-tools/CLAUDE.md) - CLIs for schema upgrade + resource / lineage migration.
- [`dolphinscheduler-microbench`](dolphinscheduler-microbench/CLAUDE.md) - JMH micro-benchmarks.
- [`dolphinscheduler-yarn-aop`](dolphinscheduler-yarn-aop/CLAUDE.md) - AspectJ weaver capturing YARN ApplicationIds.

### Frontend & E2E

- [`dolphinscheduler-ui`](dolphinscheduler-ui/CLAUDE.md) - Vue 3 frontend.
- [`dolphinscheduler-e2e`](dolphinscheduler-e2e/CLAUDE.md) - Selenium browser tests.

---

## Architecture overview

A **user** hits the UI, which calls the API server. The API server writes to the **metadata DB** and, for runtime operations (start / kill / pause workflow), talks to the **master** over RPC. The master consumes `t_ds_command` rows, runs the workflow state machine, and dispatches tasks to **workers**. Workers execute task plugins (shell, SQL, Spark, ...) and stream lifecycle events back to master. Failures and SLA breaches flow to the **alert server**, which fans out through alert plugins. **Registry** (Zookeeper / Etcd / JDBC) provides service discovery, leader election, and distributed locks. **Storage plugins** back the resource center and distributed-task artifacts. **Quartz** (via scheduler plugin) fires scheduled workflows, which become new `Command` rows.

## Where things live (quick lookup)

| Looking for... | Start here |
|----------------|------------|
| A REST endpoint | `dolphinscheduler-api/src/main/java/.../api/controller/` |
| Workflow execution logic | `dolphinscheduler-master/src/main/java/.../server/master/engine/` |
| Task execution logic | `dolphinscheduler-worker` + the specific `task-plugin/<type>` |
| How "X" is stored | `dolphinscheduler-dao/src/main/java/.../dao/entity/` |
| SQL schema / upgrade | `dolphinscheduler-dao/src/main/resources/sql/` |
| RPC contract between servers | `dolphinscheduler-extract/dolphinscheduler-extract-<role>` |
| UI page source | `dolphinscheduler-ui/src/views/<feature>/` |
| API call in the UI | `dolphinscheduler-ui/src/service/modules/<resource>.ts` |
| Version of a dependency | `dolphinscheduler-bom/pom.xml` |

## Project-wide conventions

- **Formatting**: Run `./mvnw spotless:apply` before every commit/push. Spotless covers Java sources, `pom.xml`, and Markdown files; CI runs `./mvnw spotless:check` and will fail PRs that are not formatted. Java imports are ordered; license headers are enforced.
- **Commit style**: `[Type-ISSUE_ID][Scope] Subject`, e.g. `[Fix-18168][Worker] ...`. All types except `Chore` require an issue ID. See [commit-message.md](docs/docs/en/contribute/join/commit-message.md) for the full convention.
- **Branching**: `dev` is the main integration branch (not `main`/`master`).
- **PRs must link a GitHub issue** and keep their scope tight: one module / one concern. For `Chore` commits, no issue ID is required by the commit convention.
- **Do not break wire / DB compatibility** silently. Changes to `extract-*` RPC interfaces, `dao` entities, enum values, and `spi.DbType` ripple to deployed clusters mid-upgrade.
- **Only one registry / storage / DB dialect is active at runtime**. Code paths that check "which one" belong inside the plugin SPI, not sprinkled through services.

## External references

- Release docs (version-specific): https://dolphinscheduler.apache.org/en-us/docs
- GitHub issues: https://github.com/apache/dolphinscheduler/issues
- Python SDK: https://dolphinscheduler.apache.org/python/main/index.html
- Contribution guide: [`docs/docs/en/contribute/join/contribute.md`](docs/docs/en/contribute/join/contribute.md)
