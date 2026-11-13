# 不向前兼容的更新

本文档记录了各版本之间不兼容的更新内容。在升级到相关版本前，请检查本文档。

## 3.0.0

* 复制和导入工作流不再添加 'copy' 后缀 [#10607](https://github.com/apache/dolphinscheduler/pull/10607)
* 使用分号作为默认的 SQL 语句分隔符 [#10869](https://github.com/apache/dolphinscheduler/pull/10869)

## 3.2.0

* 将 `common.properties` 中 `data-quality.jar.name` 属性重命名为 `data-quality.jar.dir` 并表示目录 ([#15563](https://github.com/apache/dolphinscheduler/pull/15563))
* 移除 `common.properties` 中 `data-quality.jar.name` 属性的默认键值 ([#15551](https://github.com/apache/dolphinscheduler/pull/15551))
* 移除 `StorageOperate` 的 `download()` 中的 `deleteSource` 参数 ([#14084](https://github.com/apache/dolphinscheduler/pull/14084))
* 将默认的 Unix Shell 执行器从 sh 改为 bash ([#12180](https://github.com/apache/dolphinscheduler/pull/12180))
* 从 Spark 任务中移除 Spark 版本参数 ([#11860](https://github.com/apache/dolphinscheduler/pull/11860))
* 更新了SQL任务中用于匹配变量的正则表达式 ([#13378](https://github.com/apache/dolphinscheduler/pull/13378))
* 更改了环境变量名称，将 `PYTHON_HOME` 改为 `PYTHON_LAUNCHER`， 将 `DATAX_HOME` 改为 `DATAX_LAUNCHER` ([#14523](https://github.com/apache/dolphinscheduler/pull/14523))
* 将mysql驱动版本从8.0.16升级至8.0.33 ([#14684](https://github.com/apache/dolphinscheduler/pull/14684))
* 在 /datasources/tables && /datasources/tableColumns 接口中添加了必选字段`database` [#14406](https://github.com/apache/dolphinscheduler/pull/14406)
* 资源中心相关接口删除请求参数 `description` ([#14394](https://github.com/apache/dolphinscheduler/pull/14394))

## 3.3.0

* 从 `资源中心` 中移除 `udf-manage` 功能 ([#16209](https://github.com/apache/dolphinscheduler/pull/16209))
* 从 `任务插件` 中移除 `Pigeon` 类型 ([#16218](https://github.com/apache/dolphinscheduler/pull/16218))
* 统一代码中的 `process` 为 `workflow` ([#16515](https://github.com/apache/dolphinscheduler/pull/16515))
* 废弃从 1.x 至 2.x 的升级代码  ([#16543](https://github.com/apache/dolphinscheduler/pull/16543))
* 移除 `数据质量` 模块  ([#16794](https://github.com/apache/dolphinscheduler/pull/16794))
* 在`application.yaml`中移除`registry-disconnect-strategy`配置 ([#16821](https://github.com/apache/dolphinscheduler/pull/16821))
* 在 `worker` 的 `application.yaml` 中移除 `exec-threads`，使用`physical-task-config`替代;在master的`application.yaml`中移除`master-async-task-executor-thread-pool-size`使用`logic-task-config`替代 ([#16790](https://github.com/apache/dolphinscheduler/pull/16790))
* 在 `t_ds_worker_group` 表中移除 无用的 `other_params_json` 字段 ([#16860](https://github.com/apache/dolphinscheduler/pull/16860))
* 从 `任务插件` 中移除 `Dynamic` 类型 ([#16482](https://github.com/apache/dolphinscheduler/pull/16842))

## 3.4.0

* 将数据源配置下SSH连接参数中的publicKey字段重命名为privateKey。 ([#17666](https://github.com/apache/dolphinscheduler/pull/17666))
* 添加数据表 t_ds_serial_command。 ([#17531](https://github.com/apache/dolphinscheduler/pull/17531))
* 移除 `api-server/application.yaml` 中 `python-gateway.auth-token` 的默认值。 ([#17801](https://github.com/apache/dolphinscheduler/pull/17801))
* 重构使用 ShellCommandExecutor 的任务插件 ([#17790](https://github.com/apache/dolphinscheduler/pull/17790))
* 从 `任务插件` 中移除 `Pytorch` 类型 ([#17808](https://github.com/apache/dolphinscheduler/pull/17808))，如果您仍在使用该任务类型，请在升级前删除 `t_ds_task_definition` 和 `t_ds_task_definition_log` 中 `task_type = 'PYTORCH'` 的数据。

## 3.4.1

* 移除导入导出工作流([#17940](https://github.com/apache/dolphinscheduler/issues/17940))

## 3.4.3

* 为 `t_ds_schedules` 表新增 `missed_fire_policy` 字段。现有定时默认使用 `FIRE_ALL_MISSED`，以保持原有 Quartz `IgnoreMisfires` 行为。([#18464](https://github.com/apache/dolphinscheduler/pull/18464))
* 移除已废弃的 Dynamic Task 查询接口。([#18556](https://github.com/apache/dolphinscheduler/issues/18556))
* 移除已废弃的任务及其上游关系更新接口 `PUT /projects/{projectCode}/task-definition/{code}/with-upstream`。([#18568](https://github.com/apache/dolphinscheduler/issues/18568))
* 移除已废弃的集群查询接口 `GET /cluster/query-by-code`。
* 工作流实例列表接口（`GET /projects/{projectCode}/workflow-instances`、`GET /projects/{projectCode}/workflow-instances/top-n`、`GET /projects/{projectCode}/workflow-instances/trigger`）的响应体不再返回以下属性：
  * **移除的大字段**：`commandParam`、`globalParams`、`historyCmd`、`varPool`、`stateHistory`
  * **移除的非数据库字段**：`stateDescList`、`workflowDefinition`、`dagData`、`queue`、`locations`、`dependenceScheduleTimes`
  * **移除的派生属性**：`cmdTypeIfComplement`、`complementData`（补数执行相关，如需获取请使用详情接口）
  * 如需获取这些字段，请使用详情接口 `GET /projects/{projectCode}/workflow-instances/{id}`，该接口仍返回完整的 `WorkflowInstance` 对象 ([#18444](https://github.com/apache/dolphinscheduler/pull/18444))

## 3.5.0

* 内部 RPC 认证默认关闭。升级全部服务后，再协调所有服务在各自的 `common.properties` 中开启 `rpc.authentication.enabled=true` 并配置相同的 `rpc.authentication.secret`，认证开启与关闭的服务不能互通。凭据在进程内缓存，启用认证和轮换密钥需要重启服务。参见 [RPC 认证](../security/authentication-type.md#服务间-rpc-认证)。

* Master、Worker 和 Alert Server 现在按 RPC 请求生成心跳快照，并发请求共享一秒刷新间隔的线程安全系统指标缓存。`BaseHeartBeatTask` 及 Master、Worker、Alert 的心跳任务子类已移除，自定义扩展如继承这些类，需要自行管理调度。Alert Server 取消定期写注册心跳，只在启动时注册稳定进程身份，并在重连时恢复注册。`alert.max-heartbeat-interval` 已移除，API 在收到查询请求时直接通过 RPC 获取 Alert 心跳。

* Master、Worker 和 Alert 的心跳指标改名，用于区分心跳数据请求次数与原来的定时注册心跳次数：

  |             旧指标             |                 新指标                 |
  |-----------------------------|-------------------------------------|
  | `ds.master.heartbeat.count` | `ds.master.heartbeat.request.count` |
  | `ds.worker.heartbeat.count` | `ds.worker.heartbeat.request.count` |
  | `ds.alert.heartbeat.count`  | `ds.alert.heartbeat.request.count`  |

  新计数器统计进入 `getServerMetadata()` 的调用，包括本地读取和快照生成失败的请求；其速率反映轮询和查询量。升级时需更新引用旧指标的仪表盘，并改用服务健康检查判断存活。Prometheus 导出名称也相应变化，例如 `ds_master_heartbeat_count_total` 改为 `ds_master_heartbeat_request_count_total`（Worker、Alert 同理）。

* Master、Worker 和 Alert Server 的注册节点改为只保存稳定的进程身份。Master 的两个 Cluster 各自使用单线程同步拉取对应角色的心跳，每轮结束后等待配置间隔再执行下一轮；注册信息与心跳元数据分别维护。拉取失败、返回为空或进程身份不匹配时，保留最后成功获取的元数据及其 `heartbeatReceivedTime`，不发布更新事件。Worker 继续按缓存状态参与调度，直到后续有效心跳更新状态或注册节点被移除；尚未成功获取过心跳的 Worker 不参与分配。RPC 拉取失败不会移除注册信息或触发 failover；API 在每次请求时直接查询 Master/Worker/Alert 心跳，不维护心跳缓存。Master、Worker、Alert 和 API 必须协调升级，不支持新旧版本混合滚动升级。Master 的服务间心跳数据拉取间隔通过 `master.heartbeat-fetch-interval`（默认 `3s`）配置；`max-heartbeat-interval` 改为控制 failover 标记检查间隔，不再控制心跳数据写入。节点心跳查询失败时，监控响应的 `heartBeatInfo` 和 `lastHeartbeatTime` 可能为 null，新增 `metadataStale` 表示心跳数据不可用。([#18768](https://github.com/apache/dolphinscheduler/issues/18768))

* API 心跳 RPC 查询共享 8 个线程和最多 64 个任务的队列。每次查询最多保留 8 个待完成的节点请求，在注册发现完成后共享 5 秒的总超时，排队时间也计入其中。被拒绝、过期和未完成的节点保留注册信息，心跳数据为 null，并标记 `metadataStale=true`；任意 Worker 不可用时，Worker Group 查询明确报错。查询结束时移除尚未执行的任务。运行中的 RPC 继续受原有传输超时控制，晚到结果不会修改已返回的响应。

* Alert 注册信息和心跳身份统一使用 `alert.port` 配置的 RPC 端口。显式配置 `alert.alert-server-address` 时，其端口必须与 `alert.port` 一致，否则启动校验失败。

* 查询配置型 Worker Group 时，只要任一已注册 Worker 的心跳不可用，接口就明确返回查询失败并列出受影响的 Worker 地址；分组选项与分页接口均遵循这一行为。监控查询仍返回已注册节点，并明确标记不可用的心跳数据。

* `AlertSender#getAlertData` 对 null `alertType` 增加了显式拒绝（抛 `IllegalArgumentException` 并记录错误日志），而非直接抛 `NullPointerException`。滚动升级时**必须先升级 Alert Server，再升级 Master/Worker**，否则旧 Alert Server 无法识别新增的 `alert_type` 枚举值（如 `TASK_RESULT`），会导致告警投递失败。详见[升级文档](upgrade.md#滚动升降级顺序)。([#18549](https://github.com/apache/dolphinscheduler/pull/18549))

* SQL 任务结果告警内容有大小限制：默认最多保留 30 行，超过 100 字符的字符串字段值会被截断并以省略号标记；序列化后内容总大小限制在 60000 UTF-8 字节以内（兼容 MySQL TEXT 列 65535 字节容量），超出预算的行以省略标记对象表示。以上默认值可通过 `common.properties` 中的 `sql.task.alert.max.rows`、`sql.task.alert.max.row.value.length`、`sql.task.alert.max.content.bytes` 配置调整，修改后需重启 Worker 生效。 ([#18549](https://github.com/apache/dolphinscheduler/pull/18549))

