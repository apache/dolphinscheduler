# CLAUDE.md — dolphinscheduler-dist

The **release assembler**. Produces the binary tarball (`apache-dolphinscheduler-<version>-bin.tar.gz`) and the source tarball, optionally a Docker image. Has no Java code — it is a Maven assembly + shell-script orchestration module.

## Artifacts produced

- `apache-dolphinscheduler-${version}-bin.tar.gz` — standalone-server + master + worker + api + alert + UI + tools + plugin jars + scripts + configs.
- `apache-dolphinscheduler-${version}-src.tar.gz` — ASF-compliant source tarball.
- Docker images — when built with `-P docker`.

## Assembly descriptors

`src/main/assembly/`:

- `dolphinscheduler-bin.xml` — file layout of the binary tarball.
- `dolphinscheduler-src.xml` — file layout of the source tarball.

## Scripts

`src/main/scripts/` (approx):

- `assembly-plugins.sh` — copies plugin jars into the right `lib/plugin/` subdirectories.
- `docker-build.sh` — builds Docker images (invoked when `-P docker` and Docker CLI available).

## Build

```
# Binary tarball (from repo root)
./mvnw -pl dolphinscheduler-dist -am clean package

# With Docker images
./mvnw -pl dolphinscheduler-dist -am -P docker clean package
```

Output lands in `dolphinscheduler-dist/target/`.

## Gotchas

- **`-P docker` requires a working Docker CLI + BuildKit**. CI sets `DOCKER_BUILDKIT=1` explicitly.
- **`-am` is almost always needed** (`also make`) — this module reaches into every server, plugin, UI, and tool, so their sibling modules must be built first.
- **The binary tarball layout is operator-facing**. Operators depend on `bin/`, `conf/`, `lib/`, `libs/plugin/` existing at known paths. Don't reorganize without announcing it.
- **UI is built inside `dolphinscheduler-ui`** (Node / pnpm) during `./mvnw package`; this module picks up the `dist/` folder. A broken UI build breaks the dist build.
- **Plugin jars are sorted into `libs/plugin/<family>/` by `assembly-plugins.sh`** — new plugin families need to be added to this script.
- Source tarball rules are **ASF-compliant**: excludes binaries, `.git*`, etc. Don't introduce large binaries into the repo.

## Tests

None. Verification is end-to-end via `dolphinscheduler-e2e` which runs against a built tarball.

## Related modules

- Dependencies at package-time: `dolphinscheduler-standalone-server`, `-api`, `-alert-server`, `-ui`, `-tools`.
- Plugin families are picked up transitively via the `-all` uber modules (`task-all`, `datasource-all`, `storage-all`, `alert-all`, `scheduler-all`, `registry-all`, `dao-plugin-all`).
