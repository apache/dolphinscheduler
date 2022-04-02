# Python 节点

## Overview

Python 任务类型，用于创建 Python 类型的任务并执行一系列的 Python 脚本。worker 执行该任务的时候，会生成一个临时python脚本，
并使用与租户同名的 linux 用户执行这个脚本。

## Create Task

- 点击项目管理-项目名称-工作流定义，点击"创建工作流"按钮，进入DAG编辑页面。
- 工具栏中拖动 <img src="/img/tasks/icons/python.png" width="15"/> 到画板中，即可完成创建。

## Task Parameter

- 任务名称：设置任务的名称。一个工作流定义中的节点名称是唯一的。
- 运行标志：标识这个节点是否能正常调度,如果不需要执行，可以打开禁止执行开关。
- 描述：描述该节点的功能。
- 任务优先级：worker线程数不足时，根据优先级从高到低依次执行，优先级一样时根据先进先出原则执行。
- Worker分组：任务分配给worker组的机器机执行，选择Default，会随机选择一台worker机执行。
- 环境名称：配置运行脚本的环境。
- 失败重试次数：任务失败重新提交的次数，支持下拉和手填。
- 失败重试间隔：任务失败重新提交任务的时间间隔，支持下拉和手填。
- 超时告警：勾选超时告警、超时失败，当任务超过"超时时长"后，会发送告警邮件并且任务执行失败.
- 脚本：用户开发的PYTHON程序。
- 资源：是指脚本中需要调用的资源文件列表，资源中心-文件管理上传或创建的文件。
- 自定义参数：是PYTHON局部的用户自定义参数，会替换脚本中以${变量}的内容。
- 前置任务：选择当前任务的前置任务，会将被选择的前置任务设置为当前任务的上游。

## Task Example

### Simply Print

该样例模拟了常见的简单任务，这些任务只需要简单的一两行命令就能运行起来。我们以打印一行日志为例，该任务仅会在日志文件中打印一行
"This is a demo of python task"

![demo-python-simple](/img/tasks/demo/python_ui_next.jpg)

```python
print("This is a demo of python task")
```

### Custom Parameters

该样例模拟了自定义参数任务，为了更方便的复用已有的任务，或者面对动态的需求时，我们会使用变量保证脚本的复用性。本例中，我们先在自定义脚本
中定义了参数 "param_key"，并将他的值设置为 "param_val"。接着在"脚本"中使用了 print 函数，将参数 "param_key" 打印了出来。当我们保存
并运行任务后，在日志中会看到将参数 "param_key" 对应的值 "param_val" 打印出来。

![demo-python-custom-param](/img/tasks/demo/python_custom_param_ui_next.jpg)

```python
print("${param_key}")
```

## Notice

None
