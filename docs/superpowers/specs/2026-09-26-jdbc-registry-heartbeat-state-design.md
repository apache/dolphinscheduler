# JDBC Registry 心跳状态机修复设计

## 背景

JDBC Registry 客户端的 heartbeat 记录可能在数据库不可用超过 session timeout 后被其他服务清理。数据库恢复后，原服务继续尝试更新已经不存在的记录；如果忽略 `updateById` 的 0 行结果，服务会继续运行并保留过期身份。

本次修复需要同时处理 heartbeat 失效、服务关闭竞态和状态转换的 CAS 失败，避免服务在关闭后被恢复为工作状态，也避免回调与实际状态不一致。

## 状态约束

- `INIT` 只能转换为 `STARTED`。
- heartbeat 失败时，`STARTED` 进入 `SUSPENDED`；如果从上次成功 heartbeat 起已经超过 session timeout，则直接进入 `DISCONNECTED`。
- `SUSPENDED` 在 heartbeat 成功后进入 `STARTED`，并只在 CAS 成功时触发重连回调。
- `SUSPENDED` 或 `STARTED` 在 session timeout 后进入 `DISCONNECTED`，并只在 CAS 成功时触发断连回调。
- `STOPPED` 是关闭终态，任何未完成的 heartbeat 结果都不能覆盖它。
- `close()` 只有成功将当前状态转换为 `STOPPED` 的线程执行调度器关闭、数据库清理和本地集合清理。

## 实现方案

使用现有的 `AtomicReference<JdbcRegistryServerState>` 作为唯一状态存储和可见性边界。`close()` 使用 CAS 循环处理重复调用和并发状态变化；heartbeat 的成功、失败和断连路径在调用点直接处理 CAS 结果，删除只包装单个 CAS 的转换方法。断连路径只尝试允许的源状态，CAS 失败时根据当前状态结束本次事件，不通过无限重试把过期事件应用到新的状态上。

heartbeat 成功后的本地时间戳和成功日志只在当前服务仍可工作时更新。heartbeat 失败首先区分瞬时失败和已经超过 session timeout 的失败；记录不存在属于更新失败，不能通过 upsert 恢复旧身份。

## 测试方案

增加或调整以下回归覆盖：

- heartbeat 记录被清理后的 `STARTED -> SUSPENDED -> DISCONNECTED` 路径。
- 首次失败时已经超过 session timeout 的直接断连路径。
- `close()` 与失败、成功 heartbeat 结果的双向竞态，确保 `STOPPED` 最终状态不被覆盖且不会发出错误回调。
- 每个 CAS 失败分支都不会修改终态、重复触发回调或记录虚假的成功 heartbeat。
- 多客户端刷新时前一个客户端成功、后一个客户端失败的状态和时间戳行为。
- 已断连服务不再刷新 heartbeat。

验证使用 JDBC registry 模块的目标单元测试、Spotless 检查和必要的编译检查。
