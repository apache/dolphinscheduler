# Incompatible

This document records the incompatible updates between each version. You need to check this document before you upgrade to related version.

## dev

* Upgrade mysql driver version from 8.0.16 to 8.0.33 ([#14684](https://github.com/apache/dolphinscheduler/pull/14684))
* Change env `PYTHON_HOME` to `PYTHON_LAUNCHER` and `DATAX_HOME` to `DATAX_LAUNCHER` ([#14523](https://github.com/apache/dolphinscheduler/pull/14523))
* Change regex matching sql params in SQL task plugin ([#13378](https://github.com/apache/dolphinscheduler/pull/13378))
* Remove the spark version of spark task ([#11860](https://github.com/apache/dolphinscheduler/pull/11860)).
* Change the default unix shell executor from sh to bash ([#12180](https://github.com/apache/dolphinscheduler/pull/12180)).
* Remove `deleteSource` in `download()` of `StorageOperate` ([#14084](https://github.com/apache/dolphinscheduler/pull/14084))

## 3.2.0

* Remove parameter `description` from public interfaces of new resource center  ([#14394](https://github.com/apache/dolphinscheduler/pull/14394))

## 3.0.0

* Copy and import workflow without 'copy' suffix [#10607](https://github.com/apache/dolphinscheduler/pull/10607)
* Use semicolon as default sql segment separator [#10869](https://github.com/apache/dolphinscheduler/pull/10869)

## 3.2.0

* Add required field `database` in /datasources/tables && /datasources/tableColumns Api [#14406](https://github.com/apache/dolphinscheduler/pull/14406)

