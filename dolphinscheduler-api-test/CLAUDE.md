# CLAUDE.md — dolphinscheduler-api-test

Integration test harness for the REST API. **Not** bundled into the release — this module exists to run curl-style black-box tests against a full DolphinScheduler stack booted via Docker Compose + Testcontainers.

**This is a Maven parent POM.** This module is **not** declared in the root `pom.xml` `<modules>` — run it explicitly (e.g. `mvn -pl dolphinscheduler-api-test test -am`) or via CI workflows.

## Sub-modules

- **`dolphinscheduler-api-test-core`** — the reusable framework:
  - `@DolphinScheduler` annotation (marks a test class, accepts `composeFiles`).
  - `DolphinSchedulerExtension` — JUnit 5 extension that starts the Compose stack, injects `RequestClient` and page objects.
  - `RequestClient` — thin HTTP client with session handling.
  - Page-object base classes (`LoginPage`, `WorkflowDefinitionPage`, `ProjectPage`, `TenantPage`, `UserPage`, `WorkerGroupPage`).
- **`dolphinscheduler-api-test-case`** — the actual test classes. `WorkflowDefinitionAPITest`, `TenantAPITest`, `SchedulerAPITest`, etc. Each class uses `@DolphinScheduler(composeFiles = { "docker/basic/docker-compose.yaml" })` to declare its environment.

## Stack bootstrap

Compose files live under `src/test/resources/docker/<scenario>/docker-compose.yaml`:

- `basic/` — standard API + Postgres.
- `tenant/` — scenario with multi-tenant configuration.
- `oidc-login/` — API with Keycloak for OIDC flow (`realm-export.json` alongside).

The JUnit extension uses Testcontainers Compose to wait for port readiness before returning.

## Running

```
# Full suite
mvn -pl dolphinscheduler-api-test/dolphinscheduler-api-test-case test

# Single class (Mac M1)
mvn -pl dolphinscheduler-api-test/dolphinscheduler-api-test-case test \
    -Dtest=WorkflowDefinitionAPITest -Dm1_chip=true
```

Flags:

- `-Dm1_chip=true` — forces `arm64/v8` platform for Docker on Apple Silicon.
- `-Dlocal=true` — skips Testcontainers and points at a locally-running DolphinScheduler instead.

## Gotchas

- **Java 11 required** to compile this module (rest of the repo targets 1.8).
- **`@Order` is mandatory** on test methods within a class — tests are state-dependent and must run in declared order.
- **`@DisableIfTestFails` (from junit-pioneer)** cascades a failure to dependent tests in the same class; don't silently disable without understanding the chain.
- **`sessionId` is threaded through page objects**: `LoginPage.login(...)` returns a session ID that subsequent page objects must receive. Don't instantiate page objects without first logging in.
- **This module is excluded from the root reactor build** — CI drives it via a dedicated workflow. Check `.github/workflows/` before assuming a PR runs these.

## Related modules

- `dolphinscheduler-api` — the system under test.
- `dolphinscheduler-dao` — depended on to reuse entity DTOs.
- `dolphinscheduler-e2e` — complementary browser-level tests (selenium against UI).
