### Easy Scheduler

[![License](https://img.shields.io/badge/license-Apache%202-4EB1BA.svg)](https://www.apache.org/licenses/LICENSE-2.0.html)

> Easy Scheduler for Big Data

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

### 与同类调度系统的对比

|                        | EasyScheduler                                                | Azkaban                                                      | Airflow                                                      |
| :---------------------- | :------------------------------------------------------------ | :------------------------------------------------------------ | :------------------------------------------------------------ |
| <h6>稳定性               |                                                              |                                                              |                                                              |
| 单点故障 | 去中心化的多Master和多Worker | 是 <br>    单个Web和调度程序组合 | 是<br> 单一调度程序 |
| HA额外要求             | 不需要(本身就支持HA) | DB | Celery   / Dask / Mesos + Load Balancer + DB  |
| 过载处理               | 任务队列机制，单个机器上可调度的任务数量可以灵活配置，当任务过多时会缓存在任务队列中，不会造成机器卡死 | 任务太多时会卡死服务器 | 任务太多时会卡死服务器                                       |
| <h6>易用性             | | | |
| DAG监控界面            | 任务状态、任务类型、重试次数、任务运行机器、可视化变量等关键信息一目了然 | 只能看到任务状态  | 不能直观区分任务类型  |
| 可视化流程定义          | 是  <br>    所有流程定义操作都是可视化的，通过拖拽任务来绘制DAG,配置数据源及资源。同时对于第三方系统，提供api方式的操作。 | 否   <br>   通过自定义DSL绘制DAG并打包上传  | 否  <br> 通过python代码来绘制DAG，使用不便，特别是对不会写代码的业务人员基本无法使用。 |
| 快速部署               | 一键部署 | 集群化部署复杂 | 集群化部署复杂  |
| <h6>功能               | | | |
| 是否能暂停和恢复        | 支持暂停，恢复操作 | 否  <br>  需将工作流杀死再运行  | 否 <br>   需将工作流杀死再运行  |
| 是否支持多租户          | 支持  <br>    easyscheduler上的用户可以通过租户和hadoop用户实现多对一或一对一的映射关系，这对大数据作业的调度是非常重要的。 | 否   | 否  |
| 任务类型               | 支持传统的shell任务，同时支持大数据平台任务调度：   MR、Spark、SQL(mysql、postgresql、hive、sparksql)、Python、Procedure、Sub_Process | shell、gobblin、hadoopJava、java、hive、pig、spark、hdfsToTeradata、teradataToHdfs | BashOperator、DummyOperator、MySqlOperator、HiveOperator、EmailOperator、HTTPOperator、SqlOperator |
| 契合度                 | 支持大数据作业spark,hive,mr的调度，同时由于支持多租户，与大数据业务更加契合 | 由于不支持多租户，在大数据平台业务使用不够灵活               | 由于不支持多租户，在大数据平台业务使用不够灵活               |
| <h6>扩展性             |  |  |  |
| <center>是否支持自定义任务类型 | 是 | 是 | 是 |
| 是否支持集群扩展        | 是   <br>   调度器使用分布式调度，整体的调度能力会随便集群的规模线性增长，Master和Worker支持动态上下线 | 是，但是复杂   <br>   Executor水平扩展 | 是，但是复杂 <br>  Executor水平扩展  |


### 系统部分截图

![](http://geek.analysys.cn/static/upload/47/2019-03-06/76db3013-8e3b-4d17-b167-2aa1e6a6b0ad.jpeg)

![](http://geek.analysys.cn/static/upload/47/2019-03-06/08b79a19-4aa0-4a73-a71b-81ad210513fb.jpeg)

![](http://geek.analysys.cn/static/upload/47/2019-03-06/384dd8a3-4cf8-4e3e-944d-1185ba198f75.jpeg)

### 文档
- 部署文档
<a href="https://analysys.github.io/EasyScheduler/pages/deploy-background.html" target="_blank">后端部署文档</a>

<a href="https://analysys.github.io/EasyScheduler/pages/deploy-foreground.html" target="_blank">前端部署文档</a>

[**使用手册**](https://analysys.github.io/EasyScheduler/pages/guide-manual.html?_blank "使用手册") 

更多文档请参考 XXX

### 帮助
The fastest way to get response from our developers is to submit issues,   or add our wechat : 510570367

