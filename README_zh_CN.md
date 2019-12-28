Dolphin Scheduler
============
[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/apache/Incubator-DolphinScheduler?category=lines)](https://github.com/apache/Incubator-DolphinScheduler)
[![codecov](https://codecov.io/gh/apache/incubator-dolphinscheduler/branch/dev/graph/badge.svg)](https://codecov.io/gh/apache/incubator-dolphinscheduler/branch/dev)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache-dolphinscheduler&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache-dolphinscheduler)

> Dolphin Scheduler for Big Data



[![Stargazers over time](https://starchart.cc/apache/incubator-dolphinscheduler.svg)](https://starchart.cc/apache/incubator-dolphinscheduler)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)


**设计特点：** 一个分布式易扩展的可视化DAG工作流任务调度系统。致力于解决数据处理流程中错综复杂的依赖关系，使调度系统在数据处理流程中`开箱即用`。
其主要目标如下：
 - 以DAG图的方式将Task按照任务的依赖关系关联起来，可实时可视化监控任务的运行状态
 - 支持丰富的任务类型：Shell、MR、Spark、SQL(mysql、postgresql、hive、sparksql),Python,Sub_Process、Procedure等
 - 支持工作流定时调度、依赖调度、手动调度、手动暂停/停止/恢复，同时支持失败重试/告警、从指定节点恢复失败、Kill任务等操作
 - 支持工作流优先级、任务优先级及任务的故障转移及任务超时告警/失败
 - 支持工作流全局参数及节点自定义参数设置
 - 支持资源文件的在线上传/下载，管理等，支持在线文件创建、编辑
 - 支持任务日志在线查看及滚动、在线下载日志等
 - 实现集群HA，通过Zookeeper实现Master集群和Worker集群去中心化
 - 支持对`Master/Worker` cpu load，memory，cpu在线查看
 - 支持工作流运行历史树形/甘特图展示、支持任务状态统计、流程状态统计
 - 支持补数
 - 支持多租户
 - 支持国际化
 - 还有更多等待伙伴们探索


### 系统部分截图

![](http://geek.analysys.cn/static/upload/221/2019-03-29/0a9dea80-fb02-4fa5-a812-633b67035ffc.jpeg)

![](http://geek.analysys.cn/static/upload/221/2019-04-01/83686def-a54f-4169-8cae-77b1f8300cc1.png)

![](http://geek.analysys.cn/static/upload/221/2019-03-29/83c937c7-1793-4d7a-aa28-b98460329fe0.jpeg)

### 文档

- <a href="https://dolphinscheduler.apache.org/zh-cn/docs/user_doc/backend-deployment.html" target="_blank">后端部署文档</a>

- <a href="https://dolphinscheduler.apache.org/zh-cn/docs/user_doc/frontend-deployment.html" target="_blank">前端部署文档</a>

- [**使用手册**](https://dolphinscheduler.apache.org/zh-cn/docs/user_doc/system-manual.html?_blank "系统使用手册")

- [**升级文档**](https://dolphinscheduler.apache.org/zh-cn/docs/release/upgrade.html?_blank "升级文档")

- <a href="http://106.75.43.194:8888" target="_blank">我要体验</a>

更多文档请参考 <a href="https://dolphinscheduler.apache.org/zh-cn/docs/user_doc/quick-start.html" target="_blank">DolphinScheduler中文在线文档</a>


### 近期研发计划

DolphinScheduler的工作计划：<a href="https://github.com/apache/incubator-dolphinscheduler/projects/1" target="_blank">研发计划</a> ，其中 In Develop卡片下是正在研发的功能，TODO卡片是待做事项(包括 feature ideas)

### 贡献代码

非常欢迎大家来参与贡献代码，提交代码流程请参考：
[[How to contribute code](https://github.com/apache/incubator-dolphinscheduler/issues/310)]

### How to Build

```bash
mvn clean install -Prelease
```

Artifact:

```
dolphinscheduler-dist/dolphinscheduler-backend/target/apache-dolphinscheduler-incubating-${latest.release.version}-dolphinscheduler-backend-bin.tar.gz: Binary package of DolphinScheduler-Backend
dolphinscheduler-dist/dolphinscheduler-front/target/apache-dolphinscheduler-incubating-${latest.release.version}-dolphinscheduler-front-bin.tar.gz: Binary package of DolphinScheduler-UI
dolphinscheduler-dist/dolphinscheduler-src/target/apache-dolphinscheduler-incubating-${latest.release.version}-src.zip: Source code package of DolphinScheduler
```

### 感谢

Dolphin Scheduler使用了很多优秀的开源项目，比如google的guava、guice、grpc，netty，ali的bonecp，quartz，以及apache的众多开源项目等等，
正是由于站在这些开源项目的肩膀上，才有Dolphin Scheduler的诞生的可能。对此我们对使用的所有开源软件表示非常的感谢！我们也希望自己不仅是开源的受益者，也能成为开源的
贡献者，于是我们决定把易调度贡献出来，并承诺长期维护。也希望对开源有同样热情和信念的伙伴加入进来，一起为开源献出一份力！


### 获得帮助
1. Submit an issue
1. Mail list: dev@dolphinscheduler.apache.org. Mail to dev-subscribe@dolphinscheduler.apache.org, follow the reply to subscribe the mail list.
1. Contact WeChat group manager, ID 510570367. This is for Mandarin(CN) discussion.

### 版权
Please refer to [LICENSE](https://github.com/apache/incubator-dolphinscheduler/blob/dev/LICENSE) file.








