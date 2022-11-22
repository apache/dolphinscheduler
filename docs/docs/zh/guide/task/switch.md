# Switch

Switch 是一个条件判断节点，依据[全局变量](../parameter/global.md)的值和用户所编写的表达式判断结果执行对应分支。
**注意**使用 javax.script.ScriptEngine.eval 执行表达式。

## 创建任务

点击项目管理 -> 项目名称 -> 工作流定义，点击"创建工作流"按钮，进入 DAG 编辑页面。
拖动工具栏中的 <img src="../../../../img/switch.png" width="20"/> 任务节点到画板中即能完成任务创建。
**注意** switch 任务创建后，要先配置上下游，才能配置任务分支的参数。

## 任务参数

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (- 默认参数说明请参考[DolphinScheduler任务参数附录]&#40;appendix.md#默认任务参数&#41;`默认任务参数`一栏。)

- 默认参数说明请参考[DolphinScheduler任务参数附录](appendix.md)`默认任务参数`一栏。

| **任务参数** |                                 **描述**                                  |
|----------|-------------------------------------------------------------------------|
| 条件       | 可以为 switch 任务配置多个条件，当条件满足时，就会执行指定的分支，可以配置多个不同的条件来满足不同的业务，使用字符串判断时需要使用"" |
| 分支流转     | 默认的流转内容，当**条件**中的内容为全部不符合要求时，则运行**分支流转**中指定的分支                          |

## 任务样例

这里使用一个 switch 任务以及三个 shell 任务来演示。

### 创建工作流

新建 switch 任务，以及下游的三个 shell 任务。shell 任务没有要求。
switch 任务需要和下游任务连线配置关系后，才可以进行下游任务的选择。

![switch_01](../../../../img/tasks/demo/switch_01.png)

### 设置条件

配置条件和默认分支，满足条件会走指定分支，都不满足则走默认分支。
图中如果变量的值为 "A" 则执行分支 taskA，如果变量的值为 "B" 则执行分支 taskB ，都不满足则执行 default。

![switch_02](../../../../img/tasks/demo/switch_02.png)

条件使用了全局变量，请参考[全局变量](../parameter/global.md)。
这里配置全局变量的值为 A。

![switch_03](../../../../img/tasks/demo/switch_03.png)

如果执行正确，那么 taskA 会被正确执行。

### 执行

执行，并且查看是否符合预期。可以看到符合预期，执行了指定的下游任务 taskA。

![switch_04](../../../../img/tasks/demo/switch_04.png)

