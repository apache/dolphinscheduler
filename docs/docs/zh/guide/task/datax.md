# DATAX 节点

## 综述

DataX 任务类型，用于执行 DataX 程序。对于 DataX 节点，worker 会通过执行 `${DATAX_HOME}/bin/datax.py` 来解析传入的 json 文件。

## 创建任务

- 点击项目管理 -> 项目名称 -> 工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的<img src="/img/tasks/icons/datax.png" width="15"/> 任务节点到画板中。

## 任务参数

- 节点名称：设置任务节点的名称。一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个结点是否能正常调度，如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：worker 线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- Worker 分组：任务分配给 worker 组的机器执行，选择 Default ，会随机选择一台 worker 机执行。
- 环境名称：配置运行脚本的环境。
- 失败重试次数：任务失败重新提交的次数。
- 失败重试间隔：任务失败重新提交任务的时间间隔，以分为单位。
- 延时执行时间：任务延迟执行的时间，以分为单位。
- 超时警告：勾选超时警告、超时失败，当任务超过“超时时长”后，会发送告警邮件并且任务执行失败。
- 自定义模板：当默认提供的数据源不满足所需要求的时，可自定义 datax 节点的 json 配置文件内容。
- json：DataX 同步的 json 配置文件。
- 自定义参数：sql 任务类型，而存储过程是自定义参数顺序的给方法设置值自定义参数类型和数据类型同存储过程任务类型一样。区别在于SQL任务类型自定义参数会替换 sql 语句中 ${变量}。
- 数据源：选择抽取数据的数据源。
- sql 语句：目标库抽取数据的 sql 语句，节点执行时自动解析 sql 查询列名，映射为目标表同步列名，源表和目标表列名不一致时，可以通过列别名（as）转换。
- 目标库：选择数据同步的目标库。
- 目标库前置 sql：前置 sql 在 sql 语句之前执行（目标库执行）。
- 目标库后置 sql：后置 sql 在 sql 语句之后执行（目标库执行）。
- 限流（字节数）：限制查询的字节数。
- 限流（记录数）：限制查询的记录数。
- 运行内存：可根据实际生产环境配置所需的最小和最大内存。
- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

## 任务样例

该样例演示为从 Hive 数据导入到 MySQL 中。

### 在 DolphinScheduler 中配置 DataX 环境

若生产环境中要是使用到 DataX 任务类型，则需要先配置好所需的环境。配置文件如下：`/dolphinscheduler/conf/env/dolphinscheduler_env.sh`。

![datax_task01](/img/tasks/demo/datax_task01.png)

当环境配置完成之后，需要重启 DolphinScheduler。

### 配置 DataX 任务节点

由于默认的的数据源中并不包含从 Hive 中读取数据，所以需要自定义 json，可参考：[HDFS Writer](https://github.com/alibaba/DataX/blob/master/hdfswriter/doc/hdfswriter.md)。其中需要注意的是 HDFS 路径上存在分区目录，在实际情况导入数据时，分区建议进行传参，即使用自定义参数。

在编写好所需的 json 之后，可按照下图步骤进行配置节点内容。

![datax_task02](/img/tasks/demo/datax_task02.png)

### 查看运行结果

![datax_task03](/img/tasks/demo/datax_task03.png)

## 注意事项：

若默认提供的数据源不满足需求，可在自定义模板选项中，根据实际使用环境来配置 DataX 的 writer 和 reader，可参考：https://github.com/alibaba/DataX