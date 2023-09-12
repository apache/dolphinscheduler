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

