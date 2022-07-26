# Apache Zeppelin

## 综述

`Zeppelin`任务类型，用于创建并执行`Zeppelin`类型任务。worker 执行该任务的时候，会通过`Zeppelin Cient API`触发`Zeppelin Notebook Paragraph`。
点击[这里](https://zeppelin.apache.org/) 获取更多关于`Apache Zeppelin Notebook`的信息。

## 创建任务

- 点击项目管理-项目名称-工作流定义，点击"创建工作流"按钮，进入DAG编辑页面。
- 工具栏中拖动 <img src="../../../../img/tasks/icons/zeppelin.png" width="15"/> 到画板中，即可完成创建。

## 任务参数

- 任务名称：设置任务的名称。一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个节点是否能正常调度,如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：worker线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- Worker分组：任务分配给worker组的机器机执行，选择Default，会随机选择一台worker机执行。
- 失败重试次数：任务失败重新提交的次数，支持下拉和手填。
- 失败重试间隔：任务失败重新提交任务的时间间隔，支持下拉和手填。
- 超时告警：勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败.
- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。
- Zeppelin Note ID：Zeppelin Note对应的唯一ID。
- Zeppelin Paragraph ID：Zeppelin Paragraph对应的唯一ID。如果你想一次性调度整个note，这一栏不填即可。
- Zeppelin Rest Endpoint：您的Zeppelin服务的REST Endpoint。
- Zeppelin Production Note Directory：生产模式下存放克隆note的目录。
- Zeppelin Parameters: 用于传入Zeppelin Dynamic Form的参数。

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

