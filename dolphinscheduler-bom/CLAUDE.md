# CLAUDE.md — dolphinscheduler-bom

Maven **Bill of Materials**. Pins versions for every third-party library the project uses, so individual modules can `<dependency>` without specifying a version.

## What goes here, what does NOT

**Goes here**:
- `<dependencyManagement>` entries for any lib used in more than one module.
- `<properties>` pinning version numbers with consistent naming (`<netty.version>`, `<hadoop.version>`, …).
- Profile-controlled version switches (e.g. `zk-3.8` vs `zk-3.4`).

**Does NOT go here**:
- Plugin versions (those live in the root `pom.xml`).
- Dependencies specific to one plugin (e.g. Spark client version → stays in `task-spark/pom.xml`).
- Anything the module defines for itself.

## Key version properties (rough guide)

- Spring Boot: 2.7.x (pinned by root pom, referenced here).
- MyBatis Plus: 3.5.2.
- gRPC: 1.41.0.
- Netty: 4.1.53.
- Hadoop: 3.2.4 (client side).
- AspectJ: 1.9.7.
- JDBC drivers: MySQL, PostgreSQL, Oracle, SQL Server, Snowflake, Databend, …

When in doubt, **grep here first** for a dependency's current version before searching individual `pom.xml` files.

## Profiles

- `zk-3.8` (default) vs `zk-3.4` — switches Zookeeper + Curator versions. Active at build time via `-Pzk-3.4` if a legacy ZK cluster is targeted.

## Gotchas

- **`htrace-core` is explicitly excluded** from the Hadoop transitive graph as a CVE mitigation. Keep the exclusion even if someone reports the dep is "missing".
- **Duplicate property bug**: `zeppelin-client.version` was historically defined twice (lines ~120 and ~123 of an older revision). If you see a similar duplication when editing, consolidate — the last one wins silently.
- **No Java code** — `packaging: pom`. Don't add a `src/` directory.
- **Version bumps here ripple to every module**; bump deliberately and prefer one-dep-at-a-time commits for bisect-ability.

## Related modules

Every module that needs a pinned version imports this BOM in its own `<dependencyManagement>`.
