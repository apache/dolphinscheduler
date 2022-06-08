# Jupyter

## Overview

`Jupyter`任务类型，用于创建并执行`Jupyter`类型任务。worker 执行该任务的时候，会通过`papermill`执行`jupyter note`。
点击[这里](https://papermill.readthedocs.io/en/latest/) 获取更多关于`papermill`的信息。

## Conda Configuration
 
- 在`common.properties`配置`conda.path`，将其指向您的`conda.sh`。这里的`conda`应该是您用来管理您的 `papermill`和`jupyter`所在python环境的相同`conda`。
点击 [这里](https://docs.conda.io/en/latest/) 获取更多关于`conda`的信息.
- `conda.path`默认设置为`/opt/anaconda3/etc/profile.d/conda.sh`。 如果您不清楚您的`conda`环境在哪里，只需要在命令行执行`conda info | grep -i 'base environment'`即可获得。


## Create Task

- 点击项目管理-项目名称-工作流定义，点击"创建工作流"按钮，进入DAG编辑页面。
- 工具栏中拖动 <img src="../../../../img/tasks/icons/jupyter.png" width="15"/> 到画板中，即可完成创建。

## Task Parameter

- 任务名称：设置任务的名称。一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个节点是否能正常调度,如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：worker线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- Worker分组：任务分配给worker组的机器机执行，选择Default，会随机选择一台worker机执行。
- 失败重试次数：任务失败重新提交的次数，支持下拉和手填。
- 失败重试间隔：任务失败重新提交任务的时间间隔，支持下拉和手填。
- Cpu 配额: 为执行的任务分配指定的CPU时间配额，单位百分比，默认-1代表不限制，例如1个核心的CPU满载是100%，16个核心的是1600%。
- 最大内存：为执行的任务分配指定的内存大小，超过会触发OOM被Kill同时不会进行自动重试，单位MB，默认-1代表不限制。这个功能由 [task.resource.limit.state](https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/architecture/configuration.html) 控制
- 超时告警：勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败.这个功能由 [task.resource.limit.state](https://dolphinscheduler.apache.org/zh-cn/docs/latest/user_doc/architecture/configuration.html) 控制
- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。
- Conda Env Name: Conda环境名称。
- Input Note Path: 输入的jupyter note模板路径。
- Out Note Path: 输出的jupyter note路径。
- Jupyter Parameters: 用于对接jupyter note参数化的JSON格式参数。
- Kernel: Jupyter notebook 内核。
- Engine: 用于执行Jupyter note的引擎名称。
- Jupyter Execution Timeout: 对于每个jupyter notebook cell设定的超时时间。
- Jupyter Start Timeout: 对于jupyter notebook kernel设定的启动超时时间。
- Others: 传入papermill命令的其他参数。

## Task Example

### Jupyter Task Example

这个示例展示了如何创建Jupyter任务节点：

![demo-jupyter-simple](../../../../img/tasks/demo/jupyter.png)

