Easy Scheduler
============
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/analysys/EasyScheduler?category=lines)](https://github.com/analysys/EasyScheduler)

> Easy Scheduler for Big Data


[![Stargazers over time](https://starchart.cc/analysys/EasyScheduler.svg)](https://starchart.cc/analysys/EasyScheduler)

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)


### Design features: 

A distributed and easy-to-expand visual DAG workflow scheduling system. Dedicated to solving the complex dependencies in data processing, making the scheduling system `out of the box` for data processing.
Its main objectives are as follows:

 - Associate the Tasks according to the dependencies of the tasks in a DAG graph, which can visualize the running state of task in real time.
 - Support for many task types: Shell, MR, Spark, SQL (mysql, postgresql, hive, sparksql), Python, Sub_Process, Procedure, etc.
 - Support process scheduling, dependency scheduling, manual scheduling, manual pause/stop/recovery, support for failed retry/alarm, recovery from specified nodes, Kill task, etc.
 - Support process priority, task priority and task failover and task timeout alarm/failure
 - Support process global parameters and node custom parameter settings
 - Support online upload/download of resource files, management, etc. Support online file creation and editing
 - Support task log online viewing and scrolling, online download log, etc.
 - Implement cluster HA, decentralize Master cluster and Worker cluster through Zookeeper
 - Support online viewing of `Master/Worker` cpu load, memory
 - Support process running history tree/gantt chart display, support task status statistics, process status statistics
 - Support backfilling data
 - Support multi-tenant
 - Support internationalization
 - There are more waiting partners to explore


### What's in Easy Scheduler

 Stability | Easy to use | Features | Scalability |
 -- | -- | -- | --
Decentralized multi-master and multi-worker | Visualization process defines key information such as task status, task type, retry times, task running machine, visual variables and so on at a glance.  |  Support pause, recover operation | support custom task types 
HA is supported by itself | All process definition operations are visualized, dragging tasks to draw DAGs, configuring data sources and resources. At the same time, for third-party systems, the api mode operation is provided. | Users on easyscheduler can achieve many-to-one or one-to-one mapping relationship through tenants and Hadoop users, which is very important for scheduling large data jobs. | The scheduler uses distributed scheduling, and the overall scheduling capability will increase linearly with the scale of the cluster. Master and Worker support dynamic online and offline. 
Overload processing: Task queue mechanism, the number of schedulable tasks on a single machine can be flexibly configured, when too many tasks will be cached in the task queue, will not cause machine jam. | One-click deployment | Supports traditional shell tasks, and also support big data platform task scheduling: MR, Spark, SQL (mysql, postgresql, hive, sparksql), Python, Procedure, Sub_Process |  |




### System partial screenshot

![image](https://user-images.githubusercontent.com/48329107/61368744-1f5f3b00-a8c1-11e9-9cf1-10f8557a6b3b.png)

![image](https://user-images.githubusercontent.com/48329107/61368966-9dbbdd00-a8c1-11e9-8dcc-a9469d33583e.png)

![image](https://user-images.githubusercontent.com/48329107/61372146-f347b800-a8c8-11e9-8882-66e8934ada23.png)


### Document

- <a href="https://analysys.github.io/easyscheduler_docs/backend-deployment.html" target="_blank">Backend deployment documentation</a>

- <a href="https://analysys.github.io/easyscheduler_docs/frontend-deployment.html" target="_blank">Front-end deployment documentation</a>

- [**User manual**](https://analysys.github.io/easyscheduler_docs/system-manual.html?_blank "User manual") 

- [**Upgrade document**](https://analysys.github.io/easyscheduler_docs/upgrade.html?_blank "Upgrade document") 

- <a href="http://52.82.13.76:8888" target="_blank">Online Demo</a> 

More documentation please refer to <a href="https://analysys.github.io/easyscheduler_docs/" target="_blank">[EasyScheduler online documentation]</a>

### Recent R&D plan
Work plan of Easy Scheduler: [R&D plan](https://github.com/analysys/EasyScheduler/projects/1), where `In Develop` card is the features of 1.1.0 version , TODO card is to be done (including feature ideas)

### How to contribute code

Welcome to participate in contributing code, please refer to the process of submitting the code:
[[How to contribute code](https://github.com/analysys/EasyScheduler/issues/310)]

### Thanks

Easy Scheduler uses a lot of excellent open source projects, such as google guava, guice, grpc, netty, ali bonecp, quartz, and many open source projects of apache, etc.
It is because of the shoulders of these open source projects that the birth of the Easy Scheduler is possible. We are very grateful for all the open source software used! We also hope that we will not only be the beneficiaries of open source, but also be open source contributors, so we decided to contribute to easy scheduling and promised long-term updates. We also hope that partners who have the same passion and conviction for open source will join in and contribute to open source!

### Get Help
The fastest way to get response from our developers is to submit issues,  or add our wechat : 510570367

### License
Please refer to [LICENSE](https://github.com/analysys/EasyScheduler/blob/dev/LICENSE) file.
 
 







