# Apache Dolphinscheduler

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![codecov](https://codecov.io/gh/apache/dolphinscheduler/branch/dev/graph/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache-dolphinscheduler&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache-dolphinscheduler)
[![Twitter Follow](https://img.shields.io/twitter/follow/dolphinschedule.svg?style=social&label=Follow)](https://twitter.com/dolphinschedule)
[![Slack Status](https://img.shields.io/badge/slack-join_chat-white.svg?logo=slack&style=social)](https://s.apache.org/dolphinscheduler-slack)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)

## About

Apache DolphinScheduler is the modern data orchestration platform. Agile to create high performance workflow with low-code. It is also provided powerful user interface,
dedicated to solving complex task dependencies in the data pipeline and providing various types of jobs available **out of the box**

The key features for DolphinScheduler are as follows:

- Easy to deploy, provide four ways to deploy which including Standalone, Cluster, Docker and Kubernetes.
- Easy to use, workflow can be created and managed by four ways, which including Web UI, [Python SDK](https://dolphinscheduler.apache.org/python/main/index.html), Yaml file and Open API
- Highly reliable and high availability, decentralized architecture with multi-master and multi-worker, native supports horizontal scaling.
- High performance, its performance is N times faster than other orchestration platform and it can support tens of millions of tasks per day
- Cloud Native, DolphinScheduler supports orchestrating multi-cloud/data center workflow, and supports custom task type
- Versioning both workflow and workflow instance(including tasks)
- Various state control of workflow and task, support pause/stop/recover them in any time
- Multi-tenancy support
- Others like backfill support(Web UI native), permission control including project, resource and data source

## QuickStart

- For quick experience
  - Want to [start with standalone](https://dolphinscheduler.apache.org/en-us/docs/3.1.5/guide/installation/standalone)
  - Want to [start with Docker](https://dolphinscheduler.apache.org/en-us/docs/3.1.5/guide/start/docker)
- For Kubernetes
  - [Start with Kubernetes](https://dolphinscheduler.apache.org/en-us/docs/3.1.5/guide/installation/kubernetes)

## User Interface Screenshots

* **Homepage:** Project and workflow overview, including the latest workflow instance and task instance status statistics.
![home](images/home.png)

* **Workflow Definition:** Create and manage workflow by drag and drop, easy to build and maintain complex workflow, support [bulk of tasks](https://dolphinscheduler.apache.org/en-us/docs/3.1.5/introduction-to-functions_menu/task_menu) out of box.
![workflow-definition](images/workflow-definition.png)

* **Workflow Tree View:** Abstract tree structure could clearer understanding of the relationship between tasks
![workflow-tree](images/workflow-tree.png)

* **Data source:** Manage support multiple external data sources, provide unified data access capabilities for such as MySQL, PostgreSQL, Hive, Trino, etc.
![data-source](images/data-source.png)

* **Monitor:** View the status of the master, worker and database in real time, including server resource usage and load, do quick health check without logging in to the server.
![monitor](images/monitor.png)

## Suggestions & Bug Reports

Follow [this guide](https://github.com/apache/dolphinscheduler/issues/new/choose) to report your suggestions or bugs.

## Contributing

The community welcomes everyone to contribute, please refer to this page to find out more: [How to contribute](docs/docs/en/contribute/join/contribute.md),
find the good first issue in [here](https://github.com/apache/dolphinscheduler/contribute) if you are new to DolphinScheduler.

## Community

Welcome to join the Apache DolphinScheduler community by:

- Join the [DolphinScheduler Slack](https://s.apache.org/dolphinscheduler-slack) to keep in touch with the community
- Follow the [DolphinScheduler Twitter](https://twitter.com/dolphinschedule) and get the latest news
- Subscribe DolphinScheduler mail list, users@dolphinscheduler.apache.org for user and dev@dolphinscheduler.apache.org for developer

# Landscapes

<p align="center">
<br/><br/>
<img src="https://landscape.cncf.io/images/left-logo.svg" width="150"/>&nbsp;&nbsp;<img src="https://landscape.cncf.io/images/right-logo.svg" width="200"/>
<br/><br/>
DolphinScheduler enriches the <a href="https://landscape.cncf.io/?landscape=observability-and-analysis&license=apache-license-2-0">CNCF CLOUD NATIVE Landscape.</a >

</p >
