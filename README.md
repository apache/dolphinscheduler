# Apache Dolphinscheduler

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
![codecov](https://codecov.io/gh/apache/dolphinscheduler/branch/dev/graph/badge.svg)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache-dolphinscheduler&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache-dolphinscheduler)
[![Twitter Follow](https://img.shields.io/twitter/follow/dolphinschedule.svg?style=social&label=Follow)](https://twitter.com/dolphinschedule) <!-- markdown-link-check-disable-line -->
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)

## About

Apache DolphinScheduler is a modern data orchestration platform that empowers agile, low-code development of high-performance workflows.
It is dedicated to handling complex task dependencies in data pipelines and provides a wide range of built-in job types **out of the box**.

Key features for DolphinScheduler are as follows:

- Easy to deploy, providing four deployment modes including Standalone, Cluster, Docker, and Kubernetes.
- Easy to use, workflows can be created and managed via Web UI, [Python SDK](https://dolphinscheduler.apache.org/python/main/index.html) or Open API
- Highly reliable and high availability, with a decentralized, multi-master and multi-worker architecture and native support for horizontal scaling.
- High performance, its performance is several times faster than other orchestration platforms, and it is capable of handling tens of millions of tasks per day
- Cloud Native, DolphinScheduler supports orchestrating workflows across multiple clouds and data centers, and allows custom task types
- Workflow Versioning, provides version control for both workflows and individual workflow instances, including tasks.
- Flexible state control of workflows and tasks, supports pausing, stopping, and recovering them at any time.
- Multi-tenancy support
- Additional features, backfill support(Web UI native), permission control including project and data source etc.

## QuickStart

- For quick experience
  - Want to [start with standalone](https://dolphinscheduler.apache.org/en-us/docs/3.3.0-alpha/guide/installation/standalone)
  - Want to [start with Docker](https://dolphinscheduler.apache.org/en-us/docs/3.3.0-alpha/guide/start/docker)
- For Kubernetes
  - [Start with Kubernetes](https://dolphinscheduler.apache.org/en-us/docs/3.3.0-alpha/guide/installation/kubernetes)
- For Terraform
  - [Start with Terraform](deploy/terraform/README.md) 

## User Interface Screenshots

* **Homepage:** Project and workflow overview, including the latest workflow instance and task instance status statistics.
![home](images/home.png)

* **Workflow Definition:** Create and manage workflows by drag and drop, easy to build and maintain complex workflows, support [a wide range of tasks](https://dolphinscheduler.apache.org/en-us/docs/3.3.0-alpha/introduction-to-functions_menu/task_menu) out of box.
![workflow-definition](images/workflow-definition.png)

* **Workflow Tree View:** Abstract tree structure could provide a clearer understanding of task relationships
![workflow-tree](images/workflow-tree.png)

* **Data source:** Supports multiple external data sources, provides unified data access capabilities for MySQL, PostgreSQL, Hive, Trino, etc.
![data-source](images/data-source.png)

* **Monitor:** View the status of the master, worker and database in real time, including server resource usage and load, do a quick health check without logging in to the server.
![monitor](images/monitor.png)

## Suggestions & Bug Reports

Follow [this guide](https://github.com/apache/dolphinscheduler/issues/new/choose) to report your suggestions or bugs.

## Contributing

The community welcomes contributions from everyone. Please refer to this page to find out more details: [How to contribute](docs/docs/en/contribute/join/contribute.md).
Check out good first issues [here](https://github.com/apache/dolphinscheduler/contribute) if you are new to DolphinScheduler.

## Community

Welcome to join the Apache DolphinScheduler community by:

- Use [GitHub Issues](https://github.com/apache/dolphinscheduler/issues) for questions, discussions, and bug reports
- Follow the [DolphinScheduler Twitter](https://twitter.com/dolphinschedule) and get the latest news <!-- markdown-link-check-disable-line -->
- Subscribe DolphinScheduler mail list, [users@dolphinscheduler.apache.org](mailto:users-subscribe@dolphinscheduler.apache.org) for users and [dev@dolphinscheduler.apache.org](mailto:dev-subscribe@dolphinscheduler.apache.org) for developers

# Landscapes

<p align="center">
<br/><br/>
<img src="./images/cncf-landscape-white-bg.jpg" width="175" alt="cncf-landscape"/>&nbsp;&nbsp;<img src="./images/cncf-white-bg.jpg" width="200" alt="cncf-logo"/>
<br/><br/>
DolphinScheduler enriches the <a href="https://landscape.cncf.io/?item=orchestration-management--scheduling-orchestration--dolphinscheduler">CNCF CLOUD NATIVE Landscape.</a >
</p >
