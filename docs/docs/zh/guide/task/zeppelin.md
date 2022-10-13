# Apache Zeppelin

## 综述

`Zeppelin`任务类型，用于创建并执行`Zeppelin`类型任务。worker 执行该任务的时候，会通过`Zeppelin Cient API`触发`Zeppelin Notebook Paragraph`。
点击[这里](https://zeppelin.apache.org/) 获取更多关于`Apache Zeppelin Notebook`的信息。

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击"创建工作流"按钮，进入DAG编辑页面。
- 工具栏中拖动 <img src="../../../../img/tasks/icons/zeppelin.png" width="15"/> 到画板中，即可完成创建。

## 任务参数

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

|              **任务参数**              |                      **描述**                       |
|------------------------------------|---------------------------------------------------|
| Zeppelin Note ID                   | Zeppelin Note对应的唯一ID                              |
| Zeppelin Paragraph ID              | Zeppelin Paragraph对应的唯一ID。如果你想一次性调度整个note，这一栏不填即可 |
| Zeppelin Rest Endpoint             | 您的Zeppelin服务的REST Endpoint                        |
| Zeppelin Production Note Directory | 生产模式下存放克隆note的目录                                  |
| Zeppelin Parameters                | 用于传入Zeppelin Dynamic Form的参数                      |

## 生产（克隆）模式

- 填上`Zeppelin Production Note Directory`参数以启动`生产模式`。
- 在`生产模式`下，目标note会被克隆到您所填的`Zeppelin Production Note Directory`目录下。
  `Zeppelin任务插件`将会执行克隆出来的note并在执行成功后自动清除它。
  因为在此模式下，如果您不小心修改了正在被`Dolphin Scheduler`调度的note，也不会影响到生产任务的执行，
  从而提高了稳定性。
- 如果您选择不填`Zeppelin Production Note Directory`这个参数，`Zeppelin任务插件`将会执行您的原始note。
  'Zeppelin Production Note Directory'参数在格式上应该以`斜杠`开头和结尾，例如 `/production_note_directory/`。

## 任务样例

### Zeppelin Paragraph 任务样例

这个示例展示了如何创建Zeppelin Paragraph任务节点：

![demo-zeppelin-paragraph](../../../../img/tasks/demo/zeppelin.png)

![demo-get-zeppelin-id](../../../../img/tasks/demo/zeppelin_id.png)

