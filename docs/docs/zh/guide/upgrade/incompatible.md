# 不向前兼容的更新

本文档记录了各版本之间不兼容的更新内容。在升级到相关版本前，请检查本文档。

## dev

* 将mysql驱动版本从8.0.16升级至8.0.33 ([#14684](https://github.com/apache/dolphinscheduler/pull/14684))
* 更改了环境变量名称，将 `PYTHON_HOME` 改为 `PYTHON_LAUNCHER`， 将 `DATAX_HOME` 改为 `DATAX_LAUNCHER` ([#14523](https://github.com/apache/dolphinscheduler/pull/14523))
* 更新了SQL任务中用于匹配变量的正则表达式 ([#13378](https://github.com/apache/dolphinscheduler/pull/13378))
* Remove the spark version of spark task ([#11860](https://github.com/apache/dolphinscheduler/pull/11860)).
* Change the default unix shell executor from sh to bash ([#12180](https://github.com/apache/dolphinscheduler/pull/12180)).
* Remove `deleteSource` in `download()` of `StorageOperate` ([#14084](https://github.com/apache/dolphinscheduler/pull/14084))

## 3.2.0

* 资源中心相关接口删除请求参数 `description` ([#14394](https://github.com/apache/dolphinscheduler/pull/14394))

## 3.0.0

* Copy and import workflow without 'copy' suffix [#10607](https://github.com/apache/dolphinscheduler/pull/10607)
* Use semicolon as default sql segment separator [#10869](https://github.com/apache/dolphinscheduler/pull/10869)

## 3.2.0

* 在 /datasources/tables && /datasources/tableColumns 接口中添加了必选字段`database` [#14406](https://github.com/apache/dolphinscheduler/pull/14406)

## 3.3.0

* 从 `资源中心` 中移除 `udf-manage` 功能 ([#16209])(https://github.com/apache/dolphinscheduler/pull/16209)
* 从 `任务插件` 中移除 `Pigeon` 类型 ([#16218])(https://github.com/apache/dolphinscheduler/pull/16218)
* 统一代码中的 `process` 为 `workflow` ([#16515])(https://github.com/apache/dolphinscheduler/pull/16515)
* 废弃从 1.x 至 2.x 的升级代码  ([#16543])(https://github.com/apache/dolphinscheduler/pull/16543)
* 移除 `数据质量` 模块  ([#16794])(https://github.com/apache/dolphinscheduler/pull/16794)
* 在`application.yaml`中移除`registry-disconnect-strategy`配置 ([#16821])(https://github.com/apache/dolphinscheduler/pull/16821)
* 在 `worker` 的 `application.yaml` 中移除 `exec-threads`，使用`physical-task-config`替代;在master的`application.yaml`中移除`master-async-task-executor-thread-pool-size`使用`logic-task-config`替代 ([#16790])(https://github.com/apache/dolphinscheduler/pull/16790)
* 在 `t_ds_worker_group` 表中移除 无用的 `other_params_json` 字段 ([#16860])(https://github.com/apache/dolphinscheduler/pull/16860)
* 从 `任务插件` 中移除 `Dynamic` 类型 ([#16482])(https://github.com/apache/dolphinscheduler/pull/16842)

## 3.4.0

* 将数据源配置下SSH连接参数中的publicKey字段重命名为privateKey。 ([#17666])(https://github.com/apache/dolphinscheduler/pull/17666)
* 添加数据表 t_ds_serial_command。 ([#17531])(https://github.com/apache/dolphinscheduler/pull/17531)
* 移除 `api-server/application.yaml` 中 `python-gateway.auth-token` 的默认值。 ([#17801])(https://github.com/apache/dolphinscheduler/pull/17801)
* 重构使用 ShellCommandExecutor 的任务插件 ([#17790])(https://github.com/apache/dolphinscheduler/pull/17790)
* 从 `任务插件` 中移除 `Pytorch` 类型 ([#17808])(https://github.com/apache/dolphinscheduler/pull/17808)，如果您仍在使用该任务类型，请在升级前删除 `t_ds_task_definition` 和 `t_ds_task_definition_log` 中 `task_type = 'PYTORCH'` 的数据。

