# 存储过程节点

## 综述

`PROCEDURE` 任务类型，用于执行存储过程的程序

## 创建任务

- 根据选择的数据源，执行存储过程。
> 拖动工具栏中的![PNG](https://analysys.github.io/easyscheduler_docs_cn/images/toolbar_PROCEDURE.png)任务节点到画板中，如下图所示：

<p align="center">
   <img src="/img/procedure_edit.png" width="80%" />
 </p>

## 任务参数

- 节点名称：设置任务节点的名称，一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个结点是否能正常调度，如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：`Worker` 线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- `Worker` 分组：任务分配给 `Worker` 组的机器执行，选择`Default`，会随机选择一台 `Worker` 机执行。
- 环境名称：配置运行脚本的环境。
- 失败重试次数：任务失败重新提交的次数。
- 失败重试间隔：任务失败重新提交任务的时间间隔，以分为单位。
- 延时执行时间：任务延迟执行的时间，以分为单位。
- 超时警告：勾选超时警告、超时失败，当任务超过“超时时长”后，会发送告警邮件并且任务执行失败。
- 数据源：存储过程的数据源类型支持MySQL、POSTGRESQL、HIVE、SPARK、CLICKHOUSE、ORACLE、SQLSERVER、DB2和PRESTO八种，选择对应的数据源。
- sql语句：编写sql调用存储过程。
- 自定义参数：存储过程的自定义参数类型支持IN、OUT两种，数据类型支持VARCHAR、INTEGER、LONG、FLOAT、DOUBLE、DATE、TIME、TIMESTAMP、BOOLEAN九种数据类型。
- 前置sql在sql语句之前执行。

## 任务样例

###  执行 MYSQL 存储过程

该样例为 MYSQL 数据源中常见的入门类型，主要为调用 MYSQL 中的存储过程。

#### 创建数据源

![resource_upload](/img/tasks/demo/procedure_task01.png)

#### 配置 PROCEDURE 节点

根据上述参数说明，配置所需的内容即可。

![demo-mr-simple](/img/tasks/demo/procedure_task02.png)

## Notice

存储函数需要使用 sql 任务
