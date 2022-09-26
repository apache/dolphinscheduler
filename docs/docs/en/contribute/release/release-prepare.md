# Release Preparation

## Check release-docs

Compared with the last release, the `release-docs` of the current release needs to be updated to the latest, if there are dependencies and versions changes

- `dolphinscheduler-dist/release-docs/LICENSE`
- `dolphinscheduler-dist/release-docs/NOTICE`
- `dolphinscheduler-dist/release-docs/licenses`

## Update Version

For example, to release `x.y.z`, the following updates are required:

- Version in the code:
  - `sql`:
    - `dolphinscheduler_mysql.sql`: `t_ds_version` needs to be updated to x.y.z
    - `dolphinscheduler_postgre.sql`: `t_ds_version` needs to be updated to x.y.z
    - `dolphinscheduler_h2.sql`: `t_ds_version` needs to be updated to x.y.z
    - `upgrade`: whether to add`x.y.z_schema`
    - `soft_version`: need to be updated to x.y.z
  - `deploy/docker/.env`: `HUB` change to `apache`，`TAG` change to `x.y.z`
  - `deploy/kubernetes/dolphinscheduler`:
    - `Chart.yaml`: `appVersion` needs to be updated to x.y.z (`version` is helm chart version，incremented and different from x.y.z)
    - `values.yaml`: `image.tag` needs to be updated to x.y.z
  - `dolphinscheduler-python/pydolphinscheduler/setup.py`: change `version` to x.y.z
- Version in the docs:
  - Change the placeholder `<version>`(except `pom`)  to the `x.y.z` in directory `docs`
  - Add new history version
    - `docs/docs/en/history-versions.md` and `docs/docs/zh/history-versions.md`: Add the new version and link for `x.y.z`
  - `docs/configs/docsdev.js`: change `/dev/` to `/x.y.z/`, **DO NOT** change this filename, is will be auto change by website tools.

