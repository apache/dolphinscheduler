## 名词解释

在对 Apache DolphinScheduler 了解之前，我们先来认识一下调度系统常用的名词

### 名词解释

**DAG：** 全称 Directed Acyclic Graph，简称 DAG。工作流中的 Task 任务以有向无环图的形式组装起来，从入度为零的节点进行拓扑遍历，直到无后继节点为止。举例如下图：

![about-glossary](../../../img/new_ui/dev/about/glossary.png)

**流程定义**：通过拖拽任务节点并建立任务节点的关联所形成的可视化**DAG**

**流程实例**：流程实例是流程定义的实例化，可以通过手动启动或定时调度生成。每运行一次流程定义，产生一个流程实例

**任务实例**：任务实例是流程定义中任务节点的实例化，标识着某个具体的任务

**任务类型**：目前支持有 SHELL、SQL、SUB_PROCESS(子流程)、PROCEDURE、MR、SPARK、PYTHON、DEPENDENT(依赖)，同时计划支持动态插件扩展，注意：其中 **SUB_PROCESS**类型的任务需要关联另外一个流程定义，被关联的流程定义是可以单独启动执行的

**调度方式**：系统支持基于 cron 表达式的定时调度和手动调度。命令类型支持：启动工作流、从当前节点开始执行、恢复被容错的工作流、恢复暂停流程、从失败节点开始执行、补数、定时、重跑、暂停、停止、恢复等待线程。
其中 **恢复被容错的工作流** 和 **恢复等待线程** 两种命令类型是由调度内部控制使用，外部无法调用

**定时调度**：系统采用 **quartz** 分布式调度器，并同时支持cron表达式可视化的生成

**依赖**：系统不单单支持 **DAG** 简单的前驱和后继节点之间的依赖，同时还提供**任务依赖**节点，支持**流程间的自定义任务依赖**

**优先级** ：支持流程实例和任务实例的优先级，如果流程实例和任务实例的优先级不设置，则默认是先进先出

**邮件告警**：支持 **SQL任务** 查询结果邮件发送，流程实例运行结果邮件告警及容错告警通知

**失败策略**：对于并行运行的任务，如果有任务失败，提供两种失败策略处理方式，**继续**是指不管并行运行任务的状态，直到流程失败结束。**结束**是指一旦发现失败任务，则同时Kill掉正在运行的并行任务，流程失败结束

**补数**：补历史数据，支持**区间并行**和**串行**两种补数方式，其日期选择方式包括**日期范围**和**日期枚举**两种

### 模块介绍

- dolphinscheduler-master master模块，提供工作流管理和编排服务。

- dolphinscheduler-worker worker模块，提供任务执行管理服务。

- dolphinscheduler-alert 告警模块，提供 AlertServer 服务。

- dolphinscheduler-api web应用模块，提供 ApiServer 服务。

- dolphinscheduler-common 通用的常量枚举、工具类、数据结构或者基类

- dolphinscheduler-dao 提供数据库访问等操作。

- dolphinscheduler-remote 基于 netty 的客户端、服务端

- dolphinscheduler-service service模块，包含Quartz、Zookeeper、日志客户端访问服务，便于server模块和api模块调用

- dolphinscheduler-ui 前端模块

