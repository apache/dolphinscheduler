# DolphinScheduler 商业日期调度与依赖审核功能开发纪要
## 对话与架构设计总结

**项目背景**：
用户提出希望在 Apache DolphinScheduler 现有的调度引擎基础上，深度改造以支持金融场景下的“商业日期计算”（基于指定日历的T+/-N、节假日跳过，以及动态切日时间）和“依赖与信号量状态挂起功能”(CHECK_PENDING -> READY)。另外需要强调对于调度状态的可观测性记录。

**核心产出物：**
我们与用户在5个阶段内完成了整体开发动作，以下是被改造的逻辑主线：

### 阶段 1：底层模型扩展 (Database & POJO)
- 修改 `dolphinscheduler_postgresql.sql`，在 `t_ds_command` 和 `t_ds_schedules` 表增加包含 `calendar_id`、`business_date_offset`、`cutover_time`、`command_state` 及 `wait_reason` 等 8 个字段。
- 增加高级日历定义表 `t_ds_calendar` 与日历明细表 `t_ds_calendar_date`。
- 修改 `Command.java` 和 `Schedule.java` 实体类进行数据库到运行内存的ORM映射。
- 配置了 MyBatis Mapper 的 `<sql id="baseSql">` 来确保字段能被加载。

### 阶段 2：算法心智建设 (Calendar & Date Resolution Service)
- 增加了 `CalendarDate Mapper` 以利用在数据库端的原生 SQL `offset .. limit 1` 的方式高效获取到上一个或者下一个最近的可用工作日/交易日。
- 编写了 `BusinessCalendarServiceImpl`，该服务首先检查当前基准时间是否越过了用户的 `Cutover Time` (例如每天15:00作为切日点)，其次调用数据库查询偏移量。在遇到异常时自动平滑回退，计算出高精度的 `business_date`。

### 阶段 3：指令拦截与接生池改造（Command Generation Augmentation）
- 增强 `WorkflowScheduleTriggerRequest` DTO 和 `ProcessScheduleTask`（Quartz 调度端），让所有的 Trigger 触发带上日历与偏移属性。
- 在 `WorkflowScheduleTrigger` 中的核心逻辑被颠覆：新生命令再也不是一入库就会立刻被 Fetcher 拉起；它会在生成时请求 `BusinessCalendarService` 初始化出真正的业务时间 `business_date` 和到期时间 `earliest_exec_time`，且强制被打上 `command_state = 1` (PENDING) 和 `wait_reason = 0` 的隔离封条。

### 阶段 4：中央审核官与依赖决策树 (CommandDependencyEvaluator)
- 这是整个调度大盘里新增的“检阅哨”。
- 修改了 `CommandMapper.xml` 中的全量拉取查询（限定所有老矿工只抓取 `command_state = 0` 的绿灯请求）。
- 开发了 `@Component CommandDependencyEvaluator`：开启独立后台异步守护线程池，以 5 秒为一个周期不断轮询 `command_state = 1` 的延阻队列命令。
- 根据时间窗策略判断（如果现行时区物理时间超过 `earliestTimeoutTime`），一旦放宽约束即可将军衔从 PENDING (1) 解封至 READY (0)，原有的 Fetcher 就可再次工作！

### 阶段 5：参数闭环 
- 修改了 `RunWorkflowCommandHandler`：从底层 Command 传递给 `WorkflowInstance` 的 `scheduleTime` 的过程中，把此前计算好的精准 `businessDate` 强行植入，让整个 DAG 运行时拿到的全局变量 `${system.biz.date}` 正确无误。

综上，一套高度解耦、完全异步支持跨日交割/外部条件信号的“阻塞-挂起-审核-下发”先进金融级批处理骨架已成功搭建。
