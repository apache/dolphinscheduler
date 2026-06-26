# CLAUDE.md — dolphinscheduler-ui

Web frontend. **Vue 3 + Vite + TypeScript**, Naive UI as the component library, AntV X6 for the DAG editor, ECharts for dashboards. Built separately from the Java reactor and bundled into the dist tarball.

## Tech stack

- Vue 3 (Composition API) + TypeScript.
- Build: Vite (gzip compression in prod).
- State: Pinia.
- Routing: Vue Router (5 top-level route groups).
- HTTP: axios (single wrapper in `src/service/service.ts`).
- i18n: `vue-i18n` with locale files under `src/locales/{en_US,zh_CN}/`.
- UI: Naive UI (v2.33.x), AntV X6 (DAG), D3, ECharts.

## Recommended toolchain

- Node **16.x** (not 18+ as of this writing).
- `pnpm` **7.x**.

(See the module's README for current pinned versions; `.nvmrc` / `packageManager` in `package.json` are authoritative.)

## Scripts

```
pnpm install
pnpm run dev         # Vite dev server (default :5173), proxies /dolphinscheduler → backend
pnpm run build:prod  # vue-tsc type check + Vite production build → dist/
pnpm run lint        # ESLint with --fix over .ts, .tsx, .vue
pnpm run prettier    # Prettier format over src/
```

The backend URL used by `pnpm run dev` is `VITE_APP_DEV_WEB_URL` in `.env.development`. Default expects `dolphinscheduler-api` on port 12345.

## Top-level src layout

- `assets/` — static images + fonts.
- `components/` — reusable UI parts (form widgets, data-display, DAG canvas pieces).
- `layouts/` — app shell / page chrome.
- `locales/` — i18n translations (`en_US`, `zh_CN`).
- `router/` — Vue Router config, one module per top-level feature.
- `service/` — axios instance + one file per backend resource (login, dag-menu, datasource, k8s, monitor, …).
- `store/` — Pinia stores: `user`, `project`, `locales`, `theme`, `timezone`, `route`, `ui-setting`, `file`.
- `views/` — page components: `home`, `projects`, `datasource`, `monitor`, `resource`, `security`, `login`, `profile`, `password`, `about`, `ui-setting`.
- `utils/` — helpers.

## Backend integration

- Axios base URL: `/dolphinscheduler` in dev (proxied), `VITE_APP_PROD_WEB_URL` in prod (usually same-origin behind a reverse proxy).
- Request interceptor injects `sessionId` header and a `language` cookie.
- Response interceptor unwraps `{ code, msg, data }`; `code != 0` throws; `401 / 504` redirect to `/login`.
- **There is no generated OpenAPI SDK**. Backend method signatures drift independently from these TypeScript wrappers — regressions usually show as 4xx / 5xx after a backend controller change.

## i18n

Two languages today: `en_US`, `zh_CN`. Language toggle stored in the `language` cookie (`js-cookie`).

## Gotchas

- **No unit tests** are configured (no `*.spec.ts` / `*.test.ts` found). End-to-end coverage comes from `dolphinscheduler-e2e`.
- **Node version drift is the #1 breakage**. Node 18/20 with modern OpenSSL breaks older webpack/vite configs — stick to the pinned Node.
- **DAG editor (AntV X6) is the single most complex view** — `views/projects/workflow/components/dag/`. Touch carefully.
- **Gzip pre-compression in Vite** is enabled for production; when debugging why a file isn't loaded, check whether the server serves `.gz` variants.
- **The built `dist/` is what `dolphinscheduler-dist` picks up**. Ensure `pnpm run build:prod` has been run before packaging from the repo.

## Tests

None inside this module. See `dolphinscheduler-e2e` for Selenium-driven browser tests.

## Related modules

- `dolphinscheduler-api` — the backend this UI calls.
- `dolphinscheduler-dist` — bundles `dist/` into the tarball under `ui/`.
- `dolphinscheduler-e2e` — tests the integrated UI+API.
