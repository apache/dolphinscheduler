# Apache SeaTunnel

## 综述

`SeaTunnel` 任务类型，用于创建并执行 `SeaTunnel` 类型任务。worker 执行该任务的时候，会通过 `start-seatunnel-spark.sh` 或 `start-seatunnel-flink.sh` 命令解析 config 文件。
点击 [这里](https://seatunnel.apache.org/) 获取更多关于 `Apache SeaTunnel` 的信息。

## 创建任务

- 点击项目管理 -> 项目名称 -> 工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的<img src="../../../../img/tasks/icons/seatunnel.png" width="15"/> 任务节点到画板中。

## 任务参数

- 节点名称：设置任务节点的名称。一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个结点是否能正常调度，如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：worker 线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- Worker 分组：任务分配给 worker 组的机器执行，选择 Default ，会随机选择一台 worker 机执行。
- 环境名称：配置运行脚本的环境。
- 失败重试次数：任务失败重新提交的次数。
- 失败重试间隔：任务失败重新提交任务的时间间隔，以分为单位。
- Cpu 配额: 为执行的任务分配指定的CPU时间配额，单位百分比，默认-1代表不限制，例如1个核心的CPU满载是100%，16个核心的是1600%。这个功能由 [task.resource.limit.state](../../architecture/configuration.md) 控制
- 最大内存：为执行的任务分配指定的内存大小，超过会触发OOM被Kill同时不会进行自动重试，单位MB，默认-1代表不限制。这个功能由 [task.resource.limit.state](../../architecture/configuration.md) 控制
- 延时执行时间：任务延迟执行的时间，以分为单位。
- 超时警告：勾选超时警告、超时失败，当任务超过“超时时长”后，会发送告警邮件并且任务执行失败。
- 引擎：支持 FLINK 和 SPARK
    - FLINK
        - 运行模型：支持 `run` 和 `run-application` 两种模式
        - 选项参数：用于添加 Flink 引擎本身参数，例如 `-m yarn-cluster -ynm seatunnel`
    - SPARK
        - 部署方式：指定部署模式，`cluster` `client` `local`
        - Master：指定 `Master` 模型，`yarn` `local` `spark` `mesos`，其中 `spark` 和 `mesos` 需要指定 `Master` 服务地址，例如：127.0.0.1:7077
    > 点击 [这里](https://seatunnel.apache.org/docs/2.1.2/command/usage) 获取更多关于`Apache SeaTunnel command` 使用的信息
- 自定义配置：支持自定义配置或从资源中心选择配置文件 
    > 点击 [这里](https://seatunnel.apache.org/docs/2.1.2/concept/config) 获取更多关于`Apache SeaTunnel config` 文件介绍
- 脚本：在任务节点那自定义配置信息，包括四部分：`env` `source` `transform` `sink`
- 资源文件：在任务节点引用资源中心的配置文件，只可以引用一个配置文件。
- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

## 任务样例

该样例演示为使用 Flink 引擎从 Fake 源读取数据打印到控制台。

### 在 DolphinScheduler 中配置 SeaTunnel 环境

若生产环境中要是使用到 SeaTunnel 任务类型，则需要先配置好所需的环境，配置文件如下：`/dolphinscheduler/conf/env/dolphinscheduler_env.sh`。

![seatunnel_task01](../../../../img/tasks/demo/seatunnel_task01.png)

### 配置 SeaTunnel 任务节点

根据上述参数说明，配置所需的内容即可。

![seatunnel_task02](../../../../img/tasks/demo/seatunnel_task02.png)

### Config 样例

```Config

env {
  execution.parallelism = 1
}

source {
  FakeSource {
    result_table_name = "fake"
    field_name = "name,age"
  }
}

transform {
  sql {
    sql = "select name,age from fake"
  }
}

sink {
  ConsoleSink {}
}

```
