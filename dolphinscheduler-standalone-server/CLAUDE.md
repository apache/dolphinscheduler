# CLAUDE.md — dolphinscheduler-standalone-server

**Single-JVM** DolphinScheduler: master + worker + api + alert all embedded in one process with an **H2 in-memory** database. Intended for local development, smoke tests, and lightweight demos — **not** for production.

## Entry point

`StandaloneServer` — a thin `@SpringBootApplication` whose only job is to bring up the combined context.

## Main package

`org.apache.dolphinscheduler`

## Configuration

`src/main/resources/`:

- `application.yaml` — H2 in-memory URL with `MODE=MySQL` for dialect compatibility, Spring profiles, embedded server ports.
- `logback-spring.xml`.
- `start.sh`, `jvm_args_env.sh`, `dolphinscheduler_env.sh` — startup + JVM args, packaged into the tarball.

## Dependency scope

**All server + plugin dependencies are declared `provided` in this module's `pom.xml`**. The standalone jar itself ships empty of those classes. At runtime they come from the classpath assembled by `dolphinscheduler-dist`.

This means: running the jar directly (`java -jar`) without the dist's `lib/` does **not** work. Use the `start.sh` in the tarball, or run from an IDE with the reactor modules on the classpath.

## Gotchas

- **H2 loses state on restart** by default (in-memory URL). This is a feature for smoke tests; if you want persistence swap to file-based H2 or MySQL — and then this is no longer "standalone-server" in spirit.
- **`MODE=MySQL` on the H2 URL** is required; otherwise MyBatis-Plus generated SQL misbehaves.
- **Quartz tables (`QRTZ_*`) are auto-created** on first boot via the Quartz JDBC store.
- **Assembly excludes `*.yaml` and `*.xml` from the jar** — configs live in `conf/` in the tarball so operators can edit them.
- **Do not copy application.yaml changes from master/worker/api here blindly**. The standalone profile flattens ports and disables a few HA paths (leader election short-circuits).

## Tests

No `src/test/java` in this module. Standalone-server health is exercised by the UI + API test suites running against it.

## Related modules

- `dolphinscheduler-master`, `-worker`, `-api`, `-alert-server` — embedded.
- `-alert-all`, `-task-all`, `-datasource-all`, `-storage-all` — provided plugin bundles.
- `dolphinscheduler-dist` — packages this along with plugin jars into the tarball.
