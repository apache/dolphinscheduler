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

- Unit tests in `src/test/java`.
- **Integration tests live in `dolphinscheduler-api-test`** (separate module, Docker Compose + Testcontainers).

## Related modules

- `dolphinscheduler-service`, `dolphinscheduler-dao` — primary deps.
- `dolphinscheduler-extract-master` / `-worker` / `-alert` — RPC into servers.
- `dolphinscheduler-authentication` (actuator sub-module) — secures `/actuator/**`.
- `dolphinscheduler-ui` — primary consumer.
- `dolphinscheduler-api-test` — integration harness.
