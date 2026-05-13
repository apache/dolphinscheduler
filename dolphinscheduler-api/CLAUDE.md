# CLAUDE.md — dolphinscheduler-api

REST API server. Entry point for the UI and external clients (curl, Python SDK). Uses Spring Boot with **Jetty** (not Tomcat) and springdoc-openapi for Swagger UI.

## Main package

`org.apache.dolphinscheduler.api`

## Entry point

`ApiApplicationServer` — `@SpringBootApplication`. On startup it:
1. Loads `DataSourcePluginManager` and `TaskPluginManager` (plugin discovery).
2. Binds to the port in `server.port` (default 12345).
3. Starts the Py4J gateway used by the Python SDK.

## Key sub-packages

- `api.controller` — 30+ `@RestController` classes, one per domain (workflows, tasks, users, projects, tenants, resources, data sources, alerts, monitoring, …). All URLs rooted at `/dolphinscheduler/*`.
- `api.service` / `api.service.impl` — business-logic layer wrapping `dolphinscheduler-service` and adding API-level concerns (auth checks, DTO mapping).
- `api.security` — pluggable authenticators: `PASSWORD` (default), `LDAP`, `OIDC`, `CASDOOR`, `SSO`. Selected by `security.authentication.type`.
- `api.interceptor` — `LoginHandlerInterceptor` (session cookie check), `RateLimitInterceptor`, `LocaleChangeInterceptor`.
- `api.configuration` — Spring config beans: Swagger, OAuth2, task-type catalog.
- `api.dto` — request/response DTOs.
- `api.exceptions` — `ApiExceptionHandler` (`@RestControllerAdvice`) maps exceptions → structured JSON responses.

## Extension points

- `Authenticator` — implement a new one to support additional login backends (added cases go into `AuthenticationType` enum + registered in `SecurityConfig`).
- Controllers auto-pick up new task types via `TaskTypeConfiguration` reading `task-type-config.yaml` / `dynamic-task-type-config.yaml`.

## Configuration

`src/main/resources`:

- `application.yaml` — server port, datasource, registry, security mode, CORS, OpenAPI.
- `task-type-config.yaml`, `dynamic-task-type-config.yaml` — the catalog of task types exposed to the UI.
- `i18n/messages_*.properties` — English + Simplified Chinese server-side messages.
- `swagger.properties` — springdoc config (UI lives at `/dolphinscheduler/swagger-ui/index.html`).
- `logback-spring.xml` — logging.

## Gotchas

- **Jetty, not Tomcat**. `spring-boot-starter-tomcat` is excluded transitively via `dolphinscheduler-meter`. Avoid accidentally pulling Tomcat in.
- **Session-based auth**: `LoginHandlerInterceptor` reads the `sessionId` cookie. There is no JWT by default. OIDC/CASDOOR paths still set a session.
- **OIDC requires `casdoor-spring-boot-starter`**; `OAuth2Configuration` is `@ConditionalOnProperty(security.authentication.type = OIDC|CASDOOR)`.
- **Python SDK integration** uses Py4J gateway, not REST. If a Python SDK change misbehaves, check `api-server` logs for Py4J init messages.
- **Controllers mix `@PostMapping` with form params and JSON bodies inconsistently** — this is legacy. Follow whatever shape the adjacent endpoint uses rather than converting to JSON across the board.
- **Swagger annotations are required** on new endpoints (`@Operation`, `@Parameter`). Missing ones break auto-generated docs the UI team consumes.

## Tests

Unit tests live in `src/test/java`. **Integration tests live in `dolphinscheduler-api-test`** (separate module, Docker Compose + Testcontainers).

### Running unit tests from the command line

The CI flow (`.github/workflows/unit-test.yml`) runs in two phases. Mirror it locally:

**One-time setup** — install BOM + upstream deps to local m2:

```bash
export MAVEN_OPTS="-Xmx4g -XX:MetaspaceSize=256m -XX:MaxMetaspaceSize=1024m"
./mvnw install -B \
  -pl "dolphinscheduler-bom,dolphinscheduler-api" \
  -am -DskipTests=true -Dspotless.skip=true -DskipUT=true \
  -Djacoco.skip=true -Danalyze.skip=true
```

**Run tests** — use `verify` (NOT `test`), no `-am`, no `clean`:

```bash
./mvnw verify -B -pl "dolphinscheduler-api" \
  -Dmaven.test.skip=false -Dspotless.skip=true -DskipUT=false -Danalyze.skip=true \
  -Dsurefire.printSummary=true -Dsurefire.useFile=false \
  -Dsurefire.reportFormat=plain -Dsurefire.redirectTestOutputToFile=false \
  -Dsurefire.failIfNoSpecifiedTests=false \
  -Dtest='EnvironmentServiceTest#testVerifyEnvironment'
```

Drop the `-Dtest=...` filter to run the whole module.

### Why the unusual flags

- **`verify` not `test`** — root pom uses `jacoco-maven-plugin` in offline-instrument + restore-instrumented-classes mode. Only the `verify` lifecycle runs the `restore` goal, so `test` alone leaves `target/classes` instrumented and the next build fails with "Cannot process instrumented class".
- **`dolphinscheduler-bom` must be in the install `-pl`** — without it, the installed pom lacks effective dependencyManagement and downstream compile/test classpaths drop transitive deps like `commons-collections4` and `oshi-core`.
- **`-DskipTests=true` (not `-Dmaven.test.skip=true`) during install** — `maven.test.skip` skips test-source attach which breaks downstream modules that depend on the test-jar.
- **No `-am` on the test run** — `-am` re-instruments upstream modules with jacoco and triggers the same "Cannot process instrumented class" failure.
- **No `clean` between `install` and `verify`** — clean wipes `target/classes` and the next compile re-resolves transitive deps from the (still installed) pom; some resolutions fail with `[WARNING] The POM for ... is invalid` and the build can't see `commons-collections4` etc.

### If things go sideways

- `Cannot process instrumented class` — re-run with `-Djacoco.skip=true`, or wipe `target/` for the affected module and re-run.
- `NoClassDefFoundError: oshi/SystemInfo` or `commons-collections4/CollectionUtils` at test runtime — the bom didn't get installed; redo the setup step including `dolphinscheduler-bom` in `-pl`.
- `class file contains wrong class` — local `~/.m2/.../dolphinscheduler-*-dev-SNAPSHOT.jar` is stale/corrupt. Delete the offending `dev-SNAPSHOT` directory under `~/.m2/repository/org/apache/dolphinscheduler/` and re-run install.

Surefire XML reports land at `target/surefire-reports/TEST-<class>.xml` for the per-test counts.

## Related modules

- `dolphinscheduler-service`, `dolphinscheduler-dao` — primary deps.
- `dolphinscheduler-extract-master` / `-worker` / `-alert` — RPC into servers.
- `dolphinscheduler-authentication` (actuator sub-module) — secures `/actuator/**`.
- `dolphinscheduler-ui` — primary consumer.
- `dolphinscheduler-api-test` — integration harness.
