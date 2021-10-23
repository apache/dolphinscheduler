Dolphin Scheduler Official Website
[dolphinscheduler.apache.org](https://dolphinscheduler.apache.org)
============

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/apache/dolphinscheduler?category=lines)](https://github.com/apache/dolphinscheduler)
[![codecov](https://codecov.io/gh/apache/dolphinscheduler/branch/dev/graph/badge.svg)](https://codecov.io/gh/apache/dolphinscheduler/branch/dev)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache-dolphinscheduler&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache-dolphinscheduler)
[![Twitter Follow](https://img.shields.io/twitter/follow/dolphinschedule.svg?style=social&label=Follow)](https://twitter.com/dolphinschedule)
[![Slack Status](https://img.shields.io/badge/slack-join_chat-white.svg?logo=slack&style=social)](https://join.slack.com/t/asf-dolphinscheduler/shared_invite/zt-omtdhuio-_JISsxYhiVsltmC5h38yfw)




[![Stargazers over time](https://starchart.cc/apache/dolphinscheduler.svg)](https://starchart.cc/apache/dolphinscheduler)

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)

## Design Features

DolphinScheduler is a distributed and extensible workflow scheduler platform with powerful DAG visual interfaces, dedicated to solving complex job dependencies in the data pipeline and providing various types of jobs available `out of the box`.

Its main objectives are as follows:

 - Associate the tasks according to the dependencies of the tasks in a DAG graph, which can visualize the running state of the task in real-time.
 - Support various task types: Shell, MR, Spark, SQL (MySQL, PostgreSQL, hive, spark SQL), Python, Sub_Process, Procedure, etc.
 - Support scheduling of workflows and dependencies, manual scheduling to pause/stop/recover task, support failure task retry/alarm, recover specified nodes from failure, kill task, etc.
 - Support the priority of workflows & tasks, task failover, and task timeout alarm or failure.
 - Support workflow global parameters and node customized parameter settings.
 - Support online upload/download/management of resource files, etc. Support online file creation and editing.
 - Support task log online viewing and scrolling and downloading, etc.
 - Have implemented cluster HA, decentralize Master cluster and Worker cluster through Zookeeper.
 - Support the viewing of Master/Worker CPU load, memory, and CPU usage metrics.
 - Support displaying workflow history in tree/Gantt chart, as well as statistical analysis on the task status & process status in each workflow.
 - Support back-filling data.
 - Support multi-tenant.
 - Support internationalization.
 - More features waiting for partners to explore...

## What's in DolphinScheduler

 Stability | Accessibility | Features | Scalability |
 -- | -- | -- | --
Decentralized multi-master and multi-worker | Visualization of workflow key information, such as task status, task type, retry times, task operation machine information, visual variables, and so on at a glance.  |  Support pause, recover operation | Support customized task types
support HA | Visualization of all workflow operations, dragging tasks to draw DAGs, configuring data sources and resources. At the same time, for third-party systems, provide API mode operations. | Users on DolphinScheduler can achieve many-to-one or one-to-one mapping relationship through tenants and Hadoop users, which is very important for scheduling large data jobs.  | The scheduler supports distributed scheduling, and the overall scheduling capability will increase linearly with the scale of the cluster. Master and Worker support dynamic adjustment.
Overload processing: By using the task queue mechanism, the number of schedulable tasks on a single machine can be flexibly configured. Machine jam can be avoided with high tolerance to numbers of tasks cached in task queue. | One-click deployment | Support traditional shell tasks, and big data platform task scheduling: MR, Spark, SQL (MySQL, PostgreSQL, hive, spark SQL), Python, Procedure, Sub_Process |  |

## User Interface Screenshots

![home page](https://user-images.githubusercontent.com/15833811/75218288-bf286400-57d4-11ea-8263-d639c6511d5f.jpg)
![dag](https://user-images.githubusercontent.com/15833811/75236750-3374fe80-57f9-11ea-857d-62a66a5a559d.png)
![process definition list page](https://user-images.githubusercontent.com/15833811/75216886-6f479e00-57d0-11ea-92dd-66e7640a186f.png)
![view task log online](https://user-images.githubusercontent.com/15833811/75216924-9900c500-57d0-11ea-91dc-3522a76bdbbe.png)
![resource management](https://user-images.githubusercontent.com/15833811/75216984-be8dce80-57d0-11ea-840d-58546edc8788.png)
![monitor](https://user-images.githubusercontent.com/59273635/75625839-c698a480-5bfc-11ea-8bbe-895b561b337f.png)
![security](https://user-images.githubusercontent.com/15833811/75236441-bfd2f180-57f8-11ea-88bd-f24311e01b7e.png)
![treeview](https://user-images.githubusercontent.com/15833811/75217191-3fe56100-57d1-11ea-8856-f19180d9a879.png)

## QuickStart in Docker

Please referer the official website document: [QuickStart in Docker](https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/docker-deployment.html)

## QuickStart in Kubernetes

Please referer the official website document: [QuickStart in Kubernetes](https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/kubernetes-deployment.html)

## How to Build

```bash
./mvnw clean install -Prelease
```

Artifact:

```
dolphinscheduler-dist/target/apache-dolphinscheduler-${latest.release.version}-bin.tar.gz: Binary package of DolphinScheduler
dolphinscheduler-dist/target/apache-dolphinscheduler-${latest.release.version}-src.tar.gz: Source code package of DolphinScheduler
```

## Thanks

DolphinScheduler is based on a lot of excellent open-source projects, such as Google guava, guice, grpc, netty, quartz, and many open-source projects of Apache and so on.
We would like to express our deep gratitude to all the open-source projects used in Dolphin Scheduler. We hope that we are not only the beneficiaries of open-source, but also give back to the community. Besides, we hope everyone who have the same enthusiasm and passion for open source could join in and contribute to the open-source community!

## Get Help

1. Submit an [issue](https://github.com/apache/dolphinscheduler/issues/new/choose)
1. Subscribe to this mailing list: https://dolphinscheduler.apache.org/en-us/community/development/subscribe.html, then email dev@dolphinscheduler.apache.org

## Community

You are very welcome to communicate with the developers and users of Dolphin Scheduler. There are two ways to find them:
1. Join the Slack channel by [this invitation link](https://join.slack.com/t/asf-dolphinscheduler/shared_invite/zt-omtdhuio-_JISsxYhiVsltmC5h38yfw).
2. Follow the [Twitter account of DolphinScheduler](https://twitter.com/dolphinschedule) and get the latest news on time.

### Contributor over time
  
[![Contributor over time](https://contributor-graph-api.apiseven.com/contributors-svg?chart=contributorOverTime&repo=apache/dolphinscheduler)](https://www.apiseven.com/en/contributor-graph?chart=contributorOverTime&repo=apache/dolphinscheduler) 

## How to Contribute

The community welcomes everyone to contribute, please refer to this page to find out more: [How to contribute](https://dolphinscheduler.apache.org/en-us/community/development/contribute.html).

## License

Please refer to the [LICENSE](https://github.com/apache/dolphinscheduler/blob/dev/LICENSE) file.
