# JDBC Registry 心跳状态机修复实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标：** 修复 JDBC Registry heartbeat 失效后的状态转换、关闭竞态和 CAS 失败处理，保证 `STOPPED` 终态、回调和 session timeout 语义一致。

**架构：** 继续使用 `AtomicReference<JdbcRegistryServerState>` 作为唯一状态存储。所有状态变化使用明确的 CAS；heartbeat 调用点处理 CAS 成功和失败，`close()` 只有成功进入 `STOPPED` 的线程执行清理。

**技术栈：** Java、JUnit 5、Mockito、Maven、Spotless。

## 全局约束

- `STOPPED` 是关闭终态，任何未完成的 heartbeat 结果都不能覆盖它。
- 只有成功的状态转换才触发连接回调。
- heartbeat 记录不存在时不能 upsert 或恢复旧身份。
- `DISCONNECTED` 只能由 session timeout 触发。
- 不引入新的生产依赖。

### 任务 1：补充状态转换和竞态回归测试

**文件：**

- 修改：`dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc/src/test/java/org/apache/dolphinscheduler/plugin/registry/jdbc/server/JdbcRegistryServerTest.java`

**接口：**

- 使用现有的 `refreshClientsHeartbeat()`、`close()`、`serverState` 和 `lastSuccessHeartbeat` 测试行为，不暴露新的生产接口。

- [ ] **步骤 1：添加首次失败已超时就断连的测试**

设置服务为 `STARTED`、`lastSuccessHeartbeat` 为超时之前，并让 `updateById()` 返回 `false`；断言一次 heartbeat 后状态为 `DISCONNECTED`，且断连回调只触发一次。

- [ ] **步骤 2：添加 heartbeat 成功结果晚于 close 的测试**

阻塞数据库更新，先完成 `close()`，再返回成功；断言最终状态为 `STOPPED`、不触发重连或断连回调，并断言 `lastSuccessHeartbeat` 未被关闭后的结果改写。

- [ ] **步骤 3：添加 heartbeat 失败结果晚于 close 的测试**

阻塞数据库更新，先完成 `close()`，再抛出异常；断言最终状态保持 `STOPPED`，不触发任何连接状态回调。

- [ ] **步骤 4：添加 CAS 失败的终态保护测试**

覆盖服务已经处于 `DISCONNECTED` 或 `STOPPED` 时收到成功、失败 heartbeat 结果的情况，断言状态不变、回调不重复、不会记录虚假的成功 heartbeat。

- [ ] **步骤 5：运行新增测试确认当前实现失败**

运行：

```bash
./mvnw -pl dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc -am -DskipITs -Dtest=JdbcRegistryServerTest -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：新增的首次超时断连或关闭后时间戳断言至少失败一项，失败原因必须来自当前状态处理逻辑，而不是测试编译错误。

### 任务 2：统一 heartbeat 状态转换和关闭语义

**文件：**

- 修改：`dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc/src/main/java/org/apache/dolphinscheduler/plugin/registry/jdbc/server/JdbcRegistryServer.java`

**接口：**

- 保持 `IJdbcRegistryServer` 和 `JdbcRegistryServerState` 不变。
- 删除只包装单个 CAS 的 `transitionToStarted()` 和 `transitionToSuspended()`。
- 保持 `refreshClientsHeartbeat()`、`close()` 和监听器接口签名不变。

- [ ] **步骤 1：将 close 改为 CAS 终态转换**

在现有 `synchronized (this)` 生命周期临界区内循环读取状态并执行 `compareAndSet(current, STOPPED)`；读取到 `STOPPED` 时直接返回。只有 CAS 成功的调用继续关闭调度器、清理数据库和清空本地集合。

- [ ] **步骤 2：将 start 的状态写入改为明确的 INIT 到 STARTED 转换**

保留启动方法的生命周期锁，在初始化完成后使用 `compareAndSet(INIT, STARTED)`，失败时不触发 `onConnected()`。

- [ ] **步骤 3：在 heartbeat 成功路径直接处理 CAS 结果**

成功更新数据库后，仅当服务仍处于 `STARTED` 或成功完成 `SUSPENDED -> STARTED` 时更新 `lastSuccessHeartbeat` 和成功日志；CAS 失败且当前状态为 `STOPPED` 或 `DISCONNECTED` 时结束本次处理。

- [ ] **步骤 4：在 heartbeat 失败路径直接处理允许的转换**

先计算距离上次成功 heartbeat 的时间；超时后依次尝试 `STARTED -> DISCONNECTED` 和 `SUSPENDED -> DISCONNECTED`，只有一次 CAS 成功才触发断连回调。未超时时只尝试 `STARTED -> SUSPENDED`，CAS 失败时根据当前终态结束本次事件。

- [ ] **步骤 5：明确时间戳的跨线程可见性**

将 `lastSuccessHeartbeat` 改为 `volatile long`，保持构造函数初始化并避免空值自动拆箱。

- [ ] **步骤 6：停止 DISCONNECTED 状态下的清理调度**

让 `purgeInvalidJdbcRegistryMetadata()` 在 `DISCONNECTED` 和 `STOPPED` 状态都直接返回，避免失去数据库 session 的服务继续清理其他客户端元数据。

- [ ] **步骤 7：运行任务 1 的测试确认通过**

运行同一 Maven 测试命令，预期 `JdbcRegistryServerTest` 全部通过且无失败、错误。

### 任务 3：补充多客户端和格式验证

**文件：**

- 修改：`dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc/src/test/java/org/apache/dolphinscheduler/plugin/registry/jdbc/server/JdbcRegistryServerTest.java`

- [ ] **步骤 1：添加多客户端部分更新失败测试**

注册两个客户端，让第一个更新成功、第二个更新返回 `false`；断言第一个本地 DTO 时间戳已更新，服务按当前 timeout 规则进入 `SUSPENDED` 或 `DISCONNECTED`，且不会触发重连回调。

- [ ] **步骤 2：运行完整目标测试**

运行：

```bash
./mvnw clean -pl dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc -am -DskipITs -Dtest=JdbcRegistryServerTest,JdbcRegistryDataChangeListenerAdapterTest -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：选定测试全部通过，Maven reactor 构建成功。

- [ ] **步骤 3：运行格式检查**

运行：

```bash
./mvnw -pl dolphinscheduler-registry/dolphinscheduler-registry-plugins/dolphinscheduler-registry-jdbc spotless:check
```

预期：Spotless 检查成功。

- [ ] **步骤 4：检查差异和工作区**

运行 `git diff --check`、`git status --short` 和目标文件差异审查，确认只包含本次状态机修复、测试和设计文档，不包含工作区原有未跟踪文件。
