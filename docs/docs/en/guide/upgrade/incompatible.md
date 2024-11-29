# Incompatible

This document records the incompatible updates between each version. You need to check this document before you upgrade to related version.

## dev

* Upgrade mysql driver version from 8.0.16 to 8.0.33 ([#14684](https://github.com/apache/dolphinscheduler/pull/14684))
* Change env `PYTHON_HOME` to `PYTHON_LAUNCHER` and `DATAX_HOME` to `DATAX_LAUNCHER` ([#14523](https://github.com/apache/dolphinscheduler/pull/14523))
* Change regex matching sql params in SQL task plugin ([#13378](https://github.com/apache/dolphinscheduler/pull/13378))
* Remove the spark version of spark task ([#11860](https://github.com/apache/dolphinscheduler/pull/11860)).
* Change the default unix shell executor from sh to bash ([#12180](https://github.com/apache/dolphinscheduler/pull/12180)).
* Remove `deleteSource` in `download()` of `StorageOperate` ([#14084](https://github.com/apache/dolphinscheduler/pull/14084))
* Remove default key for attribute `data-quality.jar.name` in `common.properties` ([#15551](https://github.com/apache/dolphinscheduler/pull/15551))
* Rename attribute `data-quality.jar.name` to `data-quality.jar.dir` in `common.properties` and represent for directory ([#15563](https://github.com/apache/dolphinscheduler/pull/15563))

## 3.2.0

* Remove parameter `description` from public interfaces of new resource center  ([#14394](https://github.com/apache/dolphinscheduler/pull/14394))

## 3.0.0

* Copy and import workflow without 'copy' suffix [#10607](https://github.com/apache/dolphinscheduler/pull/10607)
* Use semicolon as default sql segment separator [#10869](https://github.com/apache/dolphinscheduler/pull/10869)

## 3.2.0

* Add required field `database` in /datasources/tables && /datasources/tableColumns Api [#14406](https://github.com/apache/dolphinscheduler/pull/14406)

## 3.3.0

* Remove the `udf-manage` function from the `resource center` ([#16209])(https://github.com/apache/dolphinscheduler/pull/16209)
* Remove the `Pigeon` from the `Task Plugin` ([#16218])(https://github.com/apache/dolphinscheduler/pull/16218)
* Uniformly name `process` in code as `workflow` ([#16515])(https://github.com/apache/dolphinscheduler/pull/16515)
* Deprecated upgrade code of 1.x and 2.x ([#16543])(https://github.com/apache/dolphinscheduler/pull/16543)
* Remove the `Data Quality` module ([#16794])(https://github.com/apache/dolphinscheduler/pull/16794)
* Remove the `registry-disconnect-strategy` in `application.yaml` ([#16821])(https://github.com/apache/dolphinscheduler/pull/16821)
* Remove `exec-threads` in worker's `application.yaml`, please use `physical-task-config`;Remove `master-async-task-executor-thread-pool-size` in master's `application.yaml`, please use `logic-task-config` ([#16790])(https://github.com/apache/dolphinscheduler/pull/16790)

