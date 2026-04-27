# CLAUDE.md — dolphinscheduler-e2e

End-to-end **browser** tests. Selenium drives a real Chrome instance against a full DolphinScheduler stack booted with Docker Compose via Testcontainers. Complements the REST-level tests in `dolphinscheduler-api-test`.

**This is a Maven parent POM.** Like `dolphinscheduler-api-test`, it is **not** declared in the root `pom.xml` `<modules>` — run it explicitly or via dedicated CI.

## Sub-modules

- **`dolphinscheduler-e2e-core`** — the framework:
  - `@DolphinScheduler` annotation — declares `composeFiles` and Selenium setup.
  - `DolphinSchedulerExtension` — JUnit 5 extension managing Testcontainers Compose + `BrowserWebDriverContainer` (Selenium 4, headless Chrome). Records videos.
  - Page-object base classes using Selenium `@FindBy` (`LoginPage`, `NavBarPage`, `UserPage`, `TenantPage`, `SecurityPage`, …).
- **`dolphinscheduler-e2e-case`** — the test classes (`*E2ETest`) using page objects. One class per feature area.

## Test framework versions

- JUnit 5 (Jupiter).
- Selenium 4.21.
- Testcontainers 1.21 (Compose + BrowserWebDriver modules).
- AssertJ + Awaitility.

## Running

```
# Full suite (from repo root)
mvn -pl dolphinscheduler-e2e/dolphinscheduler-e2e-case test

# Single test class (Apple Silicon)
mvn -pl dolphinscheduler-e2e/dolphinscheduler-e2e-case test \
    -Dtest=UserE2ETest -Dm1_chip=true
```

Flags:

- `-Dm1_chip=true` — pulls arm64 docker images.
- `-Dlocal=true` — skip Testcontainers; point at a locally running stack (for fast iteration in the IDE).

## How a test runs

1. `@DolphinScheduler(composeFiles = {...})` starts Compose (DolphinScheduler + DB + browser container).
2. `DolphinSchedulerExtension` injects a `RemoteWebDriver` and page objects into the test.
3. `LoginPage.login(...)` authenticates, returns a session.
4. Subsequent page objects are built around that session; `@Order` sequences the steps.
5. On completion, the Selenium container ships the recorded MP4 into a temp dir.

## Gotchas

- **Chrome runs inside Docker** (Selenium BrowserWebDriverContainer); headless is implicit. You don't need a local Chromedriver.
- **Video recording is RECORD_ALL by default** — check the temp directory referenced in the Maven log for `.mp4` files when debugging a failure.
- **`@Order` is mandatory** — tests assume sequential state (just like `dolphinscheduler-api-test`).
- **`@DisableIfTestFails`** (junit-pioneer) cascades a failure to dependent tests.
- **Apple Silicon**: without `-Dm1_chip=true` the default images are amd64 and run under qemu — unbearably slow.
- **Flaky UI waits**: use `Awaitility` (already on the classpath), not `Thread.sleep`. If you see a `sleep` in a page object, consider replacing it.
- **Local mode (`-Dlocal=true`)**: you need to start the backend + UI yourself beforehand. The extension will skip Compose but still boot a local Chrome container.

## Compose files

`src/test/resources/docker/<scenario>/docker-compose.yaml` — one scenario per test class typically. Keep them minimal; only add a service if a test needs it.

## Related modules

- `dolphinscheduler-ui` — the frontend under test.
- `dolphinscheduler-api` — the backend under test.
- `dolphinscheduler-api-test` — sibling test harness at the REST layer.
- `dolphinscheduler-dist` — produces the tarball some Compose scenarios install.
