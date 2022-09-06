# ChunJun节点

## 综述

ChunJun 任务类型，用于执行 ChunJun 程序。对于 ChunJun 节点，worker 会通过执行 `${CHUNJUN_HOME}/bin/start-chunjun` 来解析传入的 json 文件。

## 创建任务

- 点击项目管理 -> 项目名称 -> 工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的<img src="../../../../img/tasks/icons/chunjun.png" width="15"/> 任务节点到画板中。

## 任务参数

- 节点名称：设置任务节点的名称。一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个结点是否能正常调度，如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：worker 线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- Worker 分组：任务分配给 worker 组的机器执行，选择 Default ，会随机选择一台 worker 机执行。
- 环境名称：配置运行脚本的环境。
- 任务组名称：任务组的名称。
- 组内优先级：一个任务组内此任务的优先级。
- 失败重试次数：任务失败重新提交的次数。
- 失败重试间隔：任务失败重新提交任务的时间间隔，以分为单位。
- 延时执行时间：任务延迟执行的时间，以分为单位。
- 超时警告：勾选超时警告、超时失败，当任务超过“超时时长”后，会发送告警邮件并且任务执行失败。
- 自定义模板：自定义 ChunJun 节点的 json 配置文件内容，当前支持此种方式。
- json：ChunJun 同步的 json 配置文件。
- 自定义参数：用户自定义参数，会替换脚本中以 ${变量} 的内容。
- 部署方式： 执行ChunJun任务的方式，比如local，standalone等。
- 选项参数： 支持 `-confProp "{\"flink.checkpoint.interval\":60000}"` 格式。
- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

## 任务样例

该样例演示为从 Hive 数据导入到 MySQL 中。

### 在 DolphinScheduler 中配置 ChunJun 环境

若生产环境中要是使用到 ChunJun 任务类型，则需要先配置好所需的环境。配置文件如下：`/dolphinscheduler/conf/env/dolphinscheduler_env.sh`。

![chunjun_task01](../../../../img/tasks/demo/chunjun_task01.png)

当环境配置完成之后，需要重启 DolphinScheduler。

### 配置 ChunJun 任务节点

从 Hive 中读取数据，所以需要自定义 json，可参考：[Hive Json Template](https://github.com/DTStack/chunjun/blob/master/chunjun-examples/json/hive/binlog_hive.json)