# Commit Message 须知

一个好的 commit message 应当一眼说清**改了什么、为什么改**。完整的来龙去脉留给关联的 Issue / Pull Request —— commit message 本身保持精炼。

## 格式

```
[Type-ISSUE_ID][Scope] Subject

(可选 body —— 用要点列出主要改动)

(可选 footer —— BREAKING CHANGE / Closes #xxx)
```

Header 必填，body 和 footer 可选。Subject 建议控制在 72 字符内，避免在 GitHub 上被截断。

### Type（必填）

|     Type      |                                        使用场景                                         |  是否必须带 Issue ID  |
|---------------|-------------------------------------------------------------------------------------|------------------|
| `Feature`     | 用户可见的新功能                                                                            | 是                |
| `Improvement` | 已有功能的增强（重构、性能、体验优化）                                                                 | 是                |
| `Fix`         | Bug 修复                                                                              | 是                |
| `Doc`         | 仅文档变动                                                                               | 是                |
| `DSIP`        | 实现某个 [DSIP](https://github.com/apache/dolphinscheduler/issues?q=label%3ADSIP) 提案的变更 | 是（对应 DSIP issue） |
| `Chore`       | 构建、CI、测试脚手架、依赖升级、零碎清理                                                               | 否                |

除 `Chore` 外的所有 type **都必须带上 Issue ID**。如果没有现成 Issue，请先创建一个；否则把它归类为 `Chore`。

### Scope（可选）

变更涉及的模块名：`Master`、`Worker`、`API`、`UI`、`TaskPlugin`、`Dao` 等。请使用真实存在的模块名。当变更确实横跨多个模块（多数 `Chore` commit 属于此类）时可以省略 `[Scope]`。

### Subject（必填）

一句简短的祈使句，描述本次改动。

- 使用祈使语气：*Add*、*Fix*、*Remove*，不要用 *Added* / *Adds*。
- 说**目的**，不要说实现细节 —— *如何改*已经体现在 diff 中。
- 句末不加句号。

### Body（可选，非平凡改动建议写）

当 Subject 无法自解释时，用要点列出主要改动。保持紧凑，一行一个改动点。详细的设计动机、权衡考量、测试方案应当写在 **Pull Request 描述**里（见 [PULL_REQUEST_TEMPLATE.md](https://github.com/apache/dolphinscheduler/blob/dev/.github/PULL_REQUEST_TEMPLATE.md)），而不是塞进 commit message。

### Footer（可选）

- `BREAKING CHANGE: <说明>` —— 用于不兼容变更。当变更破坏 RPC、数据库 schema 或公开 API 兼容性时必须填写，并在 `docs/docs/en/guide/upgrade/incompatible.md` 中追加一条记录。
- `Closes #1234` —— 合入时自动关闭对应 issue。

## 示例

推荐：

```
[Fix-18201][TaskPlugin] Fix RemoteShell task NullPointerException on empty stdout
```

```
[Improvement-18224][API] Migrate EnvironmentService to typed return values

- Replace Map<String, Object> returns with dedicated DTOs
- Update controller and unit tests accordingly
```

```
[Chore][API] Remove deprecated ProjectService#checkProjectAndAuth
```

```
[Feature-17900][Master] Support task-group priority override at runtime

- Add priority field to TaskGroupQueue
- Honor override when dispatching ready tasks

Closes #17900
```

不推荐：

- `fix bug` —— 没有 type、没有 scope、没有信息量。
- `[fix] update master` —— 大小写不规范、缺 issue ID、subject 含糊。
- `[Improvement][Master] refactor scheduler to use new state machine and also fix a NPE in worker dispatch and adjust some logs` —— 多个无关改动堆在一起，应当拆成多个 commit。

## 参考资料

- [PULL_REQUEST_TEMPLATE.md](https://github.com/apache/dolphinscheduler/blob/dev/.github/PULL_REQUEST_TEMPLATE.md)
- [Pull Request 须知](./pull-request.md)
- [Apache Geode 提交消息格式](https://cwiki.apache.org/confluence/display/GEODE/Commit+Message+Format)
- [On commit messages —— Peter Hutterer](http://who-t.blogspot.com/2009/12/on-commit-messages.html)

