# ChunJun节点

## 综述

ChunJun 任务类型，用于执行 ChunJun 程序。对于 ChunJun 节点，worker 会通过执行 `${CHUNJUN_HOME}/bin/start-chunjun` 来解析传入的 json 文件。

## 创建任务

- 点击项目管理 -> 项目名称 -> 工作流定义，点击“创建工作流”按钮，进入 DAG 编辑页面；
- 拖动工具栏的<img src="../../../../img/tasks/icons/chunjun.png" width="15"/> 任务节点到画板中。

## 任务参数

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

| **任务参数** |                           **描述**                           |
|----------|------------------------------------------------------------|
| 自定义模板    | 自定义 ChunJun 节点的 json 配置文件内容，当前支持此种方式。                      |
| json     | ChunJun 同步的 json 配置文件。                                     |
| 自定义参数    | 用户自定义参数，会替换脚本中以 ${变量} 的内容。                                 |
| 部署方式     | 执行ChunJun任务的方式，比如local，standalone等。                        |
| 选项参数     | 支持 `-confProp "{\"flink.checkpoint.interval\":60000}"` 格式。 |

## 任务样例

该样例演示为从 Hive 数据导入到 MySQL 中。

### 在 DolphinScheduler 中配置 ChunJun 环境

若生产环境中要是使用到 ChunJun 任务类型，则需要先配置好所需的环境。配置文件如下：`/dolphinscheduler/conf/env/dolphinscheduler_env.sh`。

![chunjun_task01](../../../../img/tasks/demo/chunjun_task01.png)

当环境配置完成之后，需要重启 DolphinScheduler。

### 配置 ChunJun 任务节点

从 Hive 中读取数据，所以需要自定义 json，可参考：[Hive Json Template](https://github.com/DTStack/chunjun/blob/master/chunjun-examples/json/hive/binlog_hive.json)
