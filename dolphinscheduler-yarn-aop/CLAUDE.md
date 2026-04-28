# CLAUDE.md — dolphinscheduler-yarn-aop

Tiny AspectJ module that weaves into Hadoop YARN's `YarnClientImpl.submitApplication` to capture the `ApplicationId` of every submitted YARN application. The captured IDs are written to `appInfo.log` in the working directory so that the worker (or operators) can track / kill running YARN jobs tied to a task instance.

## Main package

`org.apache.dolphinscheduler.aop`

## What it actually does

A single aspect `YarnClientAspect`:

- `@AfterReturning` on `YarnClientImpl.submitApplication(...)` — records the returned `ApplicationId` to `appInfo.log`.
- `@AfterReturning` on `YarnClientImpl.getApplicationReport(...)` with a `cflow` predicate so the pointcut only fires when called *within* a `submitApplication` context (avoids noise from general status polling).

## Weaving

- **Compile-time weaving** via `aspectj-maven-plugin`.
- The produced jar is a normal jar; YARN-based task plugins (`task-mr`, `task-spark`, `task-sqoop`, …) put it on the worker classpath.
- For runtime weaving into third-party code (e.g. the YARN client loaded by Spark's classloader), load-time weaving with `-javaagent:aspectjweaver.jar` is also supported — operators may need to enable it depending on the task plugin.

## Gotchas

- **`appInfo.log` is written to the current working directory of the worker**. Operators who run multiple workers on one host with the same `cwd` will collide — each worker should have its own `cwd`.
- **Do not add more pointcuts here**. The surface is minimal on purpose: adding broad AspectJ pointcuts to Hadoop code is fragile across YARN versions.
- **AspectJ version is pinned in `dolphinscheduler-bom`** (`aspectj.version`, currently 1.9.7). Upgrading AspectJ is sensitive — test every YARN-based task plugin.
- **Tests use AspectJ syntax to mock YARN**. `YarnClientMoc`, `YarnClientAspectMoc`, `YarnClientMocTest` let the aspect be exercised without a real YARN cluster.

## Tests

`src/test/java` — AspectJ-woven mock classes verify the aspect fires correctly.

## Related modules

- `dolphinscheduler-worker` — runtime consumer (this jar is on the worker's classpath).
- `dolphinscheduler-task-plugin` → `task-mr`, `task-spark`, `task-sqoop`, `task-hivecli` — the YARN-submitting task plugins that benefit from this.
