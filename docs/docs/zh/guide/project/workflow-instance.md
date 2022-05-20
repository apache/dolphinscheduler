# 工作流实例

## 查看工作流实例

- 点击项目管理->工作流->工作流实例，进入工作流实例页面，如下图所示：

![workflow-instance](/img/new_ui/dev/project/workflow-instance.png)
          
- 点击工作流名称，进入DAG查看页面，查看任务执行状态，如下图所示。

![instance-state](/img/new_ui/dev/project/instance-state.png)

## 查看任务日志

- 进入工作流实例页面，点击工作流名称，进入DAG查看页面，双击任务节点，如下图所示：

![instance-log01](/img/new_ui/dev/project/instance-log01.png)

- 点击"查看日志"，弹出日志弹框，如下图所示,任务实例页面也可查看任务日志，参考[任务查看日志](./task-instance.md)。

![instance-log02](/img/new_ui/dev/project/instance-log02.png)

## 查看任务历史记录

- 点击项目管理->工作流->工作流实例，进入工作流实例页面，点击工作流名称，进入工作流 DAG 页面;
- 双击任务节点，如下图所示，点击"查看历史"，跳转到任务实例页面，并展示该工作流实例运行的任务实例列表

![instance-history](/img/new_ui/dev/project/instance-history.png)

## 查看运行参数

- 点击项目管理->工作流->工作流实例，进入工作流实例页面，点击工作流名称，进入工作流 DAG 页面; 
- 点击左上角图标<img src="/img/run_params_button.png" width="35"/>，查看工作流实例的启动参数；点击图标<img src="/img/global_param.png" width="35"/>，查看工作流实例的全局参数和局部参数，如下图所示：

![instance-parameter](/img/new_ui/dev/project/instance-parameter.png)

## 工作流实例操作功能

点击项目管理->工作流->工作流实例，进入工作流实例页面，如下图所示：          

![workflow-instance](/img/new_ui/dev/project/workflow-instance.png)

- **编辑：** 只能编辑 成功/失败/停止 状态的流程。点击"编辑"按钮或工作流实例名称进入 DAG 编辑页面，编辑后点击"保存"按钮，弹出保存 DAG 弹框，如下图所示，修改流程定义信息，在弹框中勾选"是否更新工作流定义"，保存后则将实例修改的信息更新到工作流定义；若不勾选，则不更新工作流定义。
       <p align="center">
         <img src="/img/editDag.png" width="80%" />
       </p>
- **重跑：** 重新执行已经终止的流程。
- **恢复失败：** 针对失败的流程，可以执行恢复失败操作，从失败的节点开始执行。
- **停止：** 对正在运行的流程进行**停止**操作，后台会先 `kill` worker 进程,再执行 `kill -9` 操作
- **暂停：** 对正在运行的流程进行**暂停**操作，系统状态变为**等待执行**，会等待正在执行的任务结束，暂停下一个要执行的任务。
- **恢复暂停：** 对暂停的流程恢复，直接从**暂停的节点**开始运行
- **删除：** 删除工作流实例及工作流实例下的任务实例
- **甘特图：** Gantt 图纵轴是某个工作流实例下的任务实例的拓扑排序，横轴是任务实例的运行时间,如图示：         

![instance-gantt](/img/new_ui/dev/project/instance-gantt.png)
