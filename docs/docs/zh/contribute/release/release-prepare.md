# 发版准备

## 检查 release-docs

和上一个版本比较，如果有依赖及版本发生了变化，当前版本的 `release-docs` 需要被更新到最新

- `dolphinscheduler-dist/release-docs/LICENSE`
- `dolphinscheduler-dist/release-docs/NOTICE`
- `dolphinscheduler-dist/release-docs/licenses`

## 更新版本

例如要发版 `x.y.z`，需要先进行以下修改:

- 修改代码中的版本号:
  - `sql`:
    - `dolphinscheduler_mysql.sql`: `t_ds_version` 版本更新为 x.y.z
    - `dolphinscheduler_postgre.sql`: `t_ds_version` 版本更新为 x.y.z
    - `dolphinscheduler_h2.sql`: `t_ds_version` 版本更新为 x.y.z
    - `upgrade`: 是否新增 `x.y.z_schema`
    - `soft_version`: 版本更新为 x.y.z
  - `deploy/docker/.env`: `HUB` 改为 `apache`，`TAG` 改为 `x.y.z`
  - `deploy/kubernetes/dolphinscheduler`:
    - `Chart.yaml`: `appVersion` 版本更新为 x.y.z (`version` 为 helm chart 版本, 增量更新但不要设置为 x.y.z)
    - `values.yaml`: `image.tag` 版本更新为 x.y.z
  - `dolphinscheduler-python/pydolphinscheduler/setup.py`: 修改其中的 `version` 为 x.y.z
- 修改文档（docs模块）中的版本号:
  - 将 `docs` 文件夹下文件的占位符 `<version>` (除了 pom.xml 相关的) 修改成 `x.y.z`
  - 新增历史版本
    - `docs/docs/en/history-versions.md` 和 `docs/docs/zh/history-versions.md`: 增加新的历史版本为 `x.y.z`
  - 修改文档 sidebar
    - `docs/configs/docsdev.js`: 将里面的 `/dev/` 修改成 `/x.y.z/`，**不要**修改文件名称，website 仓库的 shell 脚本会对他进行修改

