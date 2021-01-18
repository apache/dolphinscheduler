Dolphin Scheduler Official Website
[dolphinscheduler.apache.org](https://dolphinscheduler.apache.org)
============
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/apache/Incubator-DolphinScheduler?category=lines)](https://github.com/apache/Incubator-DolphinScheduler)
[![codecov](https://codecov.io/gh/apache/incubator-dolphinscheduler/branch/dev/graph/badge.svg)](https://codecov.io/gh/apache/incubator-dolphinscheduler/branch/dev)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache-dolphinscheduler&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache-dolphinscheduler)


> Dolphin Scheduler for Big Data

[![Stargazers over time](https://starchart.cc/apache/incubator-dolphinscheduler.svg)](https://starchart.cc/apache/incubator-dolphinscheduler)

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)


### Design features:

Dolphin Scheduler is a distributed and easy-to-extend visual DAG workflow scheduling system. It dedicates to solving the complex dependencies in data processing to make the scheduling system `out of the box` for the data processing process.

Its main objectives are as follows:

 - Associate the tasks according to the dependencies of the tasks in a DAG graph, which can visualize the running state of the task in real-time.
 - Support many task types: Shell, MR, Spark, SQL (MySQL, PostgreSQL, hive, spark SQL), Python, Sub_Process, Procedure, etc.
 - Support process scheduling, dependency scheduling, manual scheduling, manual pause/stop/recovery, support for failed retry/alarm, recovery from specified nodes, Kill task, etc.
 - Support the priority of process & task, task failover, and task timeout alarm or failure.
 - Support process global parameters and node custom parameter settings.
 - Support online upload/download of resource files, management, etc. Support online file creation and editing.
 - Support task log online viewing and scrolling, online download log, etc.
 - Implement cluster HA, decentralize Master cluster and Worker cluster through Zookeeper.
 - Support the viewing of Master/Worker CPU load, memory, and CPU usage metrics.
 - Support presenting tree or Gantt chart of workflow history as well as the statistics results of task & process status in each workflow.
 - Support backfilling data.
 - Support multi-tenant.
 - Support internationalization.
 - There are more waiting for partners to explore...


### What's in Dolphin Scheduler

 Stability | Easy to use | Features | Scalability |
 -- | -- | -- | --
Decentralized multi-master and multi-worker | Visualization process defines key information such as task status, task type, retry times, task running machine, visual variables, and so on at a glance.  |  Support pause, recover operation | Support custom task types
HA is supported by itself | All process definition operations are visualized, dragging tasks to draw DAGs, configuring data sources and resources. At the same time, for third-party systems, the API mode operation is provided. | Users on Dolphin Scheduler can achieve many-to-one or one-to-one mapping relationship through tenants and Hadoop users, which is very important for scheduling large data jobs.  | The scheduler uses distributed scheduling, and the overall scheduling capability will increase linearly with the scale of the cluster. Master and Worker support dynamic online and offline.
Overload processing: Overload processing: By using the task queue mechanism, the number of schedulable tasks on a single machine can be flexibly configured. Machine jam can be avoided with high tolerance to numbers of tasks cached in task queue. | One-click deployment | Support traditional shell tasks, and big data platform task scheduling: MR, Spark, SQL (MySQL, PostgreSQL, hive, spark SQL), Python, Procedure, Sub_Process |  |


### System partial screenshot

![home page](https://user-images.githubusercontent.com/15833811/75218288-bf286400-57d4-11ea-8263-d639c6511d5f.jpg)
![dag](https://user-images.githubusercontent.com/15833811/75236750-3374fe80-57f9-11ea-857d-62a66a5a559d.png)
![process definition list page](https://user-images.githubusercontent.com/15833811/75216886-6f479e00-57d0-11ea-92dd-66e7640a186f.png)
![view task log online](https://user-images.githubusercontent.com/15833811/75216924-9900c500-57d0-11ea-91dc-3522a76bdbbe.png)
![resource management](https://user-images.githubusercontent.com/15833811/75216984-be8dce80-57d0-11ea-840d-58546edc8788.png)
![monitor](https://user-images.githubusercontent.com/59273635/75625839-c698a480-5bfc-11ea-8bbe-895b561b337f.png)
![security](https://user-images.githubusercontent.com/15833811/75236441-bfd2f180-57f8-11ea-88bd-f24311e01b7e.png)
![treeview](https://user-images.githubusercontent.com/15833811/75217191-3fe56100-57d1-11ea-8856-f19180d9a879.png)


### Recent R&D plan
The work plan of Dolphin Scheduler: [R&D plan](https://github.com/apache/incubator-dolphinscheduler/projects/1), which `In Develop` card shows the features that are currently being developed and TODO card lists what needs to be done(including feature ideas).

### How to contribute

Welcome to participate in contributing, please refer to this website to find out more: [[How to contribute](https://dolphinscheduler.apache.org/en-us/docs/development/contribute.html)]

### How to Build

```bash
./mvnw clean install -Prelease
```

Artifact:

```
dolphinscheduler-dist/target/apache-dolphinscheduler-incubating-${latest.release.version}-dolphinscheduler-bin.tar.gz: Binary package of DolphinScheduler
dolphinscheduler-dist/target/apache-dolphinscheduler-incubating-${latest.release.version}-src.zip: Source code package of DolphinScheduler
```

### Thanks

Dolphin Scheduler is based on a lot of excellent open-source projects, such as google guava, guice, grpc, netty, ali bonecp, quartz, and many open-source projects of Apache and so on.
We would like to express our deep gratitude to all the open-source projects which contribute to making the dream of Dolphin Scheduler comes true. We hope that we are not only the beneficiaries of open-source, but also give back to the community. Besides, we expect the partners who have the same passion and conviction to open-source will join in and contribute to the open-source community!


### Get Help
1. Submit an issue
1. Subscribe to the mail list: https://dolphinscheduler.apache.org/en-us/docs/development/subscribe.html, then email dev@dolphinscheduler.apache.org


### License
Please refer to the [LICENSE](https://github.com/apache/incubator-dolphinscheduler/blob/dev/LICENSE) file.

