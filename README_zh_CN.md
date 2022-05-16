Dolphin Scheduler Official Website
[dolphinscheduler.apache.org](https://dolphinscheduler.apache.org)
============

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)
[![Total Lines](https://tokei.rs/b1/github/apache/dolphinscheduler?category=lines)](https://github.com/apache/dolphinscheduler)
[![codecov](https://codecov.io/gh/apache/dolphinscheduler/branch/dev/graph/badge.svg)](https://codecov.io/gh/apache/dolphinscheduler/branch/dev)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=apache-dolphinscheduler&metric=alert_status)](https://sonarcloud.io/dashboard?id=apache-dolphinscheduler)


[![Stargazers over time](https://starchart.cc/apache/dolphinscheduler.svg)](https://starchart.cc/apache/dolphinscheduler)

[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README_zh_CN.md)
[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)

## 设计特点

DolphinScheduler 是一个具有强大 DAG 可视化界面的分布式可扩展工作流调度平台。致力于解决数据管道中复杂的作业依赖关系，并提供`开箱即用`的各种类型的作业。

其主要目标如下：

 - DolphinScheduler 采用去中心化的多 master 和 多 Worker 架构设计
 - 支持多租户，每天支持千万级任务
 - 支持多云/数据中心工作流管理和部署
 - 支持各种任务类型
 - 支持工作流和依赖的调度，手动调度暂停/停止/恢复任务
 - 支持失败任务重试/告警、恢复指定节点失败、杀死任务等
 - 支持根据DAG图中任务的依赖关系关联任务，可以实时可视化任务的运行状态。
 - 支持工作流和任务的优先级、任务故障转移、任务超时告警或故障
 - 支持工作流全局参数和节点自定义参数设置
 - 支持在线上传/下载/管理资源文件等
 - 支持在线文件创建和编辑
 - 支持任务日志在线查看和滚动下载等
 - 支持查看Master/Worker CPU负载、内存、CPU使用指标
 - 支持以树状图/甘特图显示工作流程历史，以及对每个工作流程中的任务状态和流程状态进行统计分析。
 - 支持回填数据
 - 更多功能等待伙伴们探索...

## 系统部分截图

![dag](./images/zh_CN/dag.png)
![data-source](./images/zh_CN/data-source.png)
![home](./images/zh_CN/home.png)
![master](./images/zh_CN/master.png)
![workflow-tree](./images/zh_CN/workflow-tree.png)

## 近期研发计划

DolphinScheduler的工作计划：<a href="https://github.com/apache/dolphinscheduler/projects/1" target="_blank">研发计划</a> ，其中 In Develop卡片下是正在研发的功能，TODO卡片是待做事项(包括 feature ideas)

## 参与贡献

非常欢迎大家来参与贡献，贡献流程请参考：
[[参与贡献](https://dolphinscheduler.apache.org/zh-cn/community/development/contribute.html)]

## 快速试用 Docker

请参考官方文档: [快速试用 Docker 部署](https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/guide/start/docker.html)

## 快速试用 Kubernetes

请参考官方文档: [快速试用 Kubernetes 部署](http://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/guide/installation/kubernetes.html)

## 如何构建

```bash
./mvnw clean install -Prelease
```

制品:

```
dolphinscheduler-dist/target/apache-dolphinscheduler-${latest.release.version}-bin.tar.gz: DolphinScheduler 二进制包
dolphinscheduler-dist/target/apache-dolphinscheduler-${latest.release.version}-src.tar.gz: DolphinScheduler 源代码包
```

## 感谢

Dolphin Scheduler使用了很多优秀的开源项目，比如google的guava、guice、grpc，netty，quartz，以及apache的众多开源项目等等，
正是由于站在这些开源项目的肩膀上，才有Dolphin Scheduler的诞生的可能。对此我们对使用的所有开源软件表示非常的感谢！我们也希望自己不仅是开源的受益者，也能成为开源的贡献者,也希望对开源有同样热情和信念的伙伴加入进来，一起为开源献出一份力！

## 获得帮助

1. 提交 [issue](https://github.com/apache/dolphinscheduler/issues/new/choose)
2. [加入slack群](https://join.slack.com/t/asf-dolphinscheduler/shared_invite/zt-omtdhuio-_JISsxYhiVsltmC5h38yfw) 并在频道 `#troubleshooting` 中提问

## 社区

1. 通过[该申请链接](https://join.slack.com/t/asf-dolphinscheduler/shared_invite/zt-omtdhuio-_JISsxYhiVsltmC5h38yfw)加入slack channel
2. 关注[Apache Dolphin Scheduler的Twitter账号](https://twitter.com/dolphinschedule)获取实时动态

## 版权

请参考 [LICENSE](https://github.com/apache/dolphinscheduler/blob/dev/LICENSE) 文件.
