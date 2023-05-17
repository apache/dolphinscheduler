# 工作流定义

## 创建工作流定义

- 点击项目管理->工作流->工作流定义，进入工作流定义页面，点击“创建工作流”按钮，进入**工作流DAG编辑**页面，如下图所示：

  ![workflow-dag](../../../../img/new_ui/dev/project/workflow-dag.png)

- 工具栏中拖拽 <img src="../../../../img/tasks/icons/shell.png" width="15"/> 到画板中，新增一个Shell任务,如下图所示：

  ![demo-shell-simple](../../../../img/tasks/demo/shell.jpg)

- **添加 Shell 任务的参数设置：**

  1. 填写“节点名称”，“描述”，“脚本”字段；
  2. “运行标志”勾选“正常”，若勾选“禁止执行”，运行工作流不会执行该任务；
  3. 选择“任务优先级”：当 worker 线程数不足时，级别高的任务在执行队列中会优先执行，相同优先级的任务按照先进先出的顺序执行；
  4. 超时告警（非必选）：勾选超时告警、超时失败，填写“超时时长”，当任务执行时间超过**超时时长**，会发送告警邮件并且任务超时失败；
  5. 资源（非必选）：资源文件是资源中心->文件管理页面创建或上传的文件，如文件名为 `test.sh`，脚本中调用资源命令为 `sh test.sh`。注意调用需要使用资源的全路径；
  6. 自定义参数（非必填）；
  7. 点击"确认添加"按钮，保存任务设置。
- **配置任务之间的依赖关系：** 点击任务节点的右侧加号连接任务；如下图所示，任务 Node_B 和任务 Node_C 并行执行，当任务 Node_A 执行完，任务 Node_B、Node_C 会同时执行。

  ![workflow-dependent](../../../../img/new_ui/dev/project/workflow-dependent.png)

- **实时任务的依赖关系：** 若DAG中包含了实时任务的组件，则实时任务的关联关系显示为虚线，在执行工作流实例的时候会跳过实时任务的执行

  ![workflow-dependent](../../../../img/new_ui/dev/project/workflow-definition-with-stream-task.png)

- **删除依赖关系：** 点击右上角"箭头"图标<img src="../../../../img/arrow.png" width="35"/>，选中连接线，点击右上角"删除"图标<img src="../../../../img/delete.png" width="35"/>，删除任务间的依赖关系。

  ![workflow-delete](../../../../img/new_ui/dev/project/workflow-delete.png)

- **保存工作流定义：** 点击”保存“按钮，弹出"设置DAG图名称"弹框，如下图所示，输入工作流定义名称，工作流定义描述，设置全局参数（选填，参考[全局参数](../parameter/global.md)），点击"添加"按钮，工作流定义创建成功。

  ![workflow-save](../../../../img/new_ui/dev/project/workflow-save.png)

  > 其他类型任务，请参考 [任务节点类型和参数设置](#TaskParamers)。 <!-- markdown-link-check-disable-line -->

- **执行策略**
- `并行`：如果对于同一个工作流定义，同时有多个工作流实例，则并行执行工作流实例。
- `串行等待`：如果对于同一个工作流定义，同时有多个工作流实例，则串行执行工作流实例。
- `串行抛弃`：如果对于同一个工作流定义，同时有多个工作流实例，则抛弃后生成的工作流实例并杀掉正在跑的实例。
- `串行优先`：如果对于同一个工作流定义，同时有多个工作流实例，则按照优先级串行执行工作流实例。

![workflow-execution-type](../../../../img/new_ui/dev/project/workflow-execution-type.png)

## 工作流定义操作功能

点击项目管理->工作流->工作流定义，进入工作流定义页面，如下图所示:

![workflow-list](../../../../img/new_ui/dev/project/workflow-list.png)

### 单个工作流支持的操作

- **编辑：** 只能编辑"下线"的工作流定义。工作流DAG编辑同创建工作流定义。
- **上线：** 工作流状态为"下线"时，上线工作流，只有"上线"状态的工作流能运行，但不能编辑。
- **下线：** 工作流状态为"上线"时，下线工作流，下线状态的工作流可以编辑，但不能运行。
- **运行：** 只有上线的工作流能运行。运行操作步骤见运行工作流
- **定时：** 只有上线的工作流能设置定时，系统自动定时调度工作流运行。创建定时后的状态为"下线"，需在定时管理页面上线定时才生效。定时操作步骤见工作流定时
- **定时管理：** 定时管理页面可编辑、上线/下线、删除定时。
- **删除：** 删除工作流定义。在同一个项目中，只能删除自己创建的工作流定义，其他用户的工作流定义不能进行删除，如果需要删除请联系创建用户或者管理员。
- **下载：** 下载工作流定义到本地。
- **复制：** 在当前项目下，根据当前工作流复制出一个新的工作流，新工作流的名称会在原工作流名称的基础上加上后缀`_copy_<date>`。
- **导出：** 导出工作流定义json文件。
- **版本信息：** 查看工作流版本信息，可在版本信息列表中切换工作流版本。
- **树形图：** 以树形结构展示任务节点的类型及任务状态，如下图所示：

![workflow-tree](../../../../img/new_ui/dev/project/workflow-tree.png)

### 工作流批量操作

选中多个工作流后，可以在工作流定义列表底部执行批量操作，如下：

- **批量删除：** 批量删除多个工作流定义。
- **批量导出：** 批量导出多个工作流定义到一个json文件。
- **批量复制：** 批量复制多个工作流定义，可选择在哪个项目下生成复制的工作流。

## 运行工作流

- 点击项目管理->工作流->工作流定义，进入工作流定义页面，如下图所示，点击"上线"按钮<img src="../../../../img/online.png" width="35"/>，上线工作流。

![workflow-online](../../../../img/new_ui/dev/project/workflow-online.png)

- 点击”运行“按钮，弹出启动参数设置弹框，如下图所示，设置启动参数，点击弹框中的"运行"按钮，工作流开始运行，工作流实例页面生成一条工作流实例。

![workflow-run](../../../../img/new_ui/dev/project/workflow-run.png)

工作流运行参数说明：

* 失败策略：当某一个任务节点执行失败时，其他并行的任务节点需要执行的策略。”继续“表示：某一任务失败后，其他任务节点正常执行；”结束“表示：终止所有正在执行的任务，并终止整个流程。
* 通知策略：当流程结束，根据流程状态发送流程执行信息通知邮件，包含任何状态都不发，成功发，失败发，成功或失败都发。
* 流程优先级：流程运行的优先级，分五个等级：最高（HIGHEST），高(HIGH),中（MEDIUM）,低（LOW），最低（LOWEST）。当 master 线程数不足时，级别高的流程在执行队列中会优先执行，相同优先级的流程按照先进先出的顺序执行。
* Worker 分组：该流程只能在指定的 worker 机器组里执行。默认是 Default，可以在任一 worker 上执行。
* 通知组：选择通知策略||超时报警||发生容错时，会发送流程信息或邮件到通知组里的所有成员。
* 启动参数: 在启动新的流程实例时，设置或覆盖全局参数的值。
* 补数：指运行指定日期范围内的工作流定义，根据补数策略生成对应的工作流实例，补数策略包括串行补数、并行补数 2 种模式。

  > 日期可以通过页面选择或者手动输入，日期范围是左关右关区间(startDate <= N <= endDate)

  * 串行补数：指定时间范围内，从开始日期至结束日期依次执行补数，依次生成多条流程实例；点击运行工作流，选择串行补数模式：例如从7月 9号到7月10号依次执行，依次在流程实例页面生成两条流程实例。

  ![workflow-serial](../../../../img/new_ui/dev/project/workflow-serial.png)

  * 并行补数： 指定时间范围内，同时进行多天的补数，同时生成多条流程实例。手动输入日期：手动输入以逗号分割日期格式为 `yyyy-MM-dd HH:mm:ss` 的日期。点击运行工作流，选择并行补数模式：例如同时执行7月9号到7月10号的工作流定义，同时在流程实例页面生成两条流程实例(执行策略为串行时流程实例按照策略执行)。

  ![workflow-parallel](../../../../img/new_ui/dev/project/workflow-parallel.png)

  * 并行度：是指在并行补数的模式下，最多并行执行的实例数。例如同时执行7月6号到7月10号的工作流定义，并行度为2，那么流程实例为：
    ![workflow-concurrency-from](../../../../img/new_ui/dev/project/workflow-concurrency-from.png)

  ![workflow-concurrency](../../../../img/new_ui/dev/project/workflow-concurrency.png)

  * 依赖模式：是否触发下游依赖节点依赖到当前工作流的工作流实例的补数（要求当前补数的工作流实例的定时状态为已上线，只会触发下游直接依赖到当前工作流的补数）。

  ![workflow-dependency](../../../../img/new_ui/dev/project/workflow-dependency.png)

  * 日期选择：

    1. 通过页面选择日期：

    ![workflow-pageSelection](../../../../img/new_ui/dev/project/workflow-pageSelection.png)

    2. 手动输入：

    ![workflow-input](../../../../img/new_ui/dev/project/workflow-input.png)

  * 补数与定时配置的关系：

    1. `未配置定时`或`已配置定时并定时状态下线`：根据所选的时间范围结合定时默认配置(每天0点)进行补数，比如该工作流调度日期为7月7号到7月10号，流程实例为：

    ![workflow-unconfiguredTimingResult](../../../../img/new_ui/dev/project/workflow-unconfiguredTimingResult.png)

    2. `已配置定时并定时状态上线`：根据所选的时间范围结合定时配置进行补数，比如该工作流调度日期为7月7号到7月10号，配置了定时（每日凌晨5点运行），流程实例为：

    ![workflow-configuredTiming](../../../../img/new_ui/dev/project/workflow-configuredTiming.png)

    ![workflow-configuredTimingResult](../../../../img/new_ui/dev/project/workflow-configuredTimingResult.png)

## 单独运行任务

- 右键选中任务，点击"启动"按钮(只有已上线的任务才能点击运行)

![workflow-task-run](../../../../img/new_ui/dev/project/workflow-task-run.png)

- 弹出启动参数设置弹框，参数说明同运行工作流

![workflow-task-run-config](../../../../img/new_ui/dev/project/workflow-task-run-config.png)

## 工作流定时

- 创建定时：点击项目管理->工作流->工作流定义，进入工作流定义页面，上线工作流，点击"定时"按钮<img src="../../../../img/timing.png" width="35"/>,弹出定时参数设置弹框，如下图所示：

  ![workflow-time01](../../../../img/new_ui/dev/project/workflow-time01.png)

- 选择起止时间。在起止时间范围内，定时运行工作流；不在起止时间范围内，不再产生定时工作流实例。

- 添加一个每隔 5 分钟执行一次的定时，如下图所示：

  ![workflow-time02](../../../../img/new_ui/dev/project/workflow-time02.png)

- 失败策略、通知策略、流程优先级、Worker 分组、通知组、收件人、抄送人同工作流运行参数。

- 点击"创建"按钮，创建定时成功，此时定时状态为"**下线**"，定时需**上线**才生效。

- 定时上线：点击"定时管理"按钮<img src="../../../../img/timeManagement.png" width="35"/>，进入定时管理页面，点击"上线"按钮，定时状态变为"上线"，如下图所示，工作流定时生效。

  ![workflow-time03](../../../../img/new_ui/dev/project/workflow-time03.png)

## 导入工作流

点击项目管理->工作流->工作流定义，进入工作流定义页面，点击"导入工作流"按钮，导入本地工作流文件，工作流定义列表显示导入的工作流，状态为下线。
