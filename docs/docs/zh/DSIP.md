# DSIP

DolphinScheduler Improvement Proposal (DSIP) 是对 Apache DolphinScheduler 代码库进行的重大改进。它不是为了小修小补存在的，
DSIP 的目的是通知社区完成或即将完成的重大变更。

## 怎样的修改应该被认定为 DSIP

- 任何重大的新功能、重大改进、引入或删除组件
- 任何公共接口的任何重大变化，例如 API接口、web ui 巨大变化

当一个 PR 或者 Issue 是否应该被认定为 DSIP 存疑时，如果有 committer 认为他应该纳入 DAIP 的范畴，那它就应该是 DSIP。

我们使用 GitHub Issue 和 Apache 邮件列表来记录和保存 DSIP，想要了解更多相关信息，您可以跳转到 当前的 DSIPs 以及 past DSIPs

作为 DSIP，它应该包含如下部分：

- 在 [dev@dolphinscheduler.apache.org][mail-to-dev] 中有一个以 `[DISCUSS][DSIP` 为开头的邮件。
- 有一个打了 "DSIP" 标签的 GitHub Issue，并在描述中包含邮链接。

### 当前的 DSIPs

当前的 DSIP 包括所有仍在进行中的 DSIP，您可以在 [当前的 DSIPs][current-DSIPs] 中找到他们

### 完结的 DSIPs

完结的 DSIP，包括所有已完成或因某种原因终止的 DSIP，您可以在 [完结的 DSIPs][past-DSIPs] 中找到他们

## DSIP 的步骤

### 创建 GitHub Issue

所有 DSIP 都应该起源于 GitHub Issue

- 如果您确定你的问题是 DSIP，你可以在 [GitHub Issue][github-issue-choose] 中点击并选择 "DSIP"
- 如果您不确定您的问题是否是 DSIP，您可以在 [GitHub Issue][github-issue-choose] 单击并选择 "Feature request"。当DolphinScheduler
  维护团队在查看 Issue 时认为他是 DSIP 时，会为 Issue 增加标签 "DSIP"。

You should and special prefix `[DSIP-XXX]`, `XXX` stand for the id DSIP. It's auto increment, and you could find the next
integer in [All DSIPs][all-DSIPs] issues.
在您的问题被标记成 DSIP 后，您应该特殊前缀 `[DSIP-XXX]`，其中`XXX` 代表 id DSIP。它是自动递增的，你可以在 [All DSIPs][all-DSIPs]
找到下一个 DSIP 的整数编号。

### 发送讨论邮件

在您的问题被标记为 "DSIP" 后，您应该发送电子邮件至 [dev@dolphinscheduler.apache.org][mail-to-dev] 描述提案的目的，以及设计草案。

下面是邮件的模板

- 标题: `[DISCUSS][DSIP-XXX] <CHANGE-TO-YOUR-LOVELY-PROPOSAL-TITLE>`, 将 `XXX` 修改为 to special integer you just change in
  GitHub Issue, and also change proposal title.
- 内容:

  ```text
  Hi community,

  <CHANGE-TO-YOUR-PROPOSAL-DETAIL>

  I already add a GitHub Issue for my proposal, which you could see in <CHANGE-TO-YOUR-GITHUB-ISSUE-LINK>.

  Looking forward any feedback for this thread.
  ```

在社区讨论并且所有人都认为它值得作为 DSIP 之后，您可以去到下节正式开始工作。但是如果社区认为它不应该是 DSIP，维护者需要终止邮件讨论并
删除 GitHub Issue 中的 "DSIP" 标签。如果当这个修改不应该合并到 DolphinScheduler 中时，维护者除了除了移除标签外，还要关闭 GitHub Issue。

### 开始开发或者为他创建子任务

当您的提案通过邮件讨论时，您可以开始工作。你可以提交一个相关的 pull requests 如果更改应该在一次提交中进行。如果提案太大，已经超过了单次
提交的范畴，你可以在 GitHub Issue 中创建子任务，如 [DSIP-1][DSIP-1]，并分成多个 pull requests 提交任务。

### 关闭 DSIP

当 DSIP 完成并合并所有相关 PR 后，您应该回复您在第二步创建的邮件讨论，通知社区 DSIP 的结果。在这之后，这个 DSIP GitHub Issue 将会被
关闭，并从 [当前的 DSIPs][current-DSIPs] 转移到 [完结的 DSIPs][past-DSIPs]，但您仍然可以在 [All DSIPs][all-DSIPs] 中找到它

## DSIP的例子

* [[DSIP-1][Feature][Parent] Add Python API for DolphinScheduler][DSIP-1]: 有多个子任务和项目。

[all-DSIPs]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+label%3A%22DSIP%22+
[current-DSIPs]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+is%3Aopen+label%3A%22DSIP%22
[past-DSIPs]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+is%3Aclosed+label%3A%22DSIP%22+
[github-issue-choose]: https://github.com/apache/dolphinscheduler/issues/new/choose
[mail-to-dev]: mailto:dev@dolphinscheduler.apache.org
[DSIP-1]: https://github.com/apache/dolphinscheduler/issues/6407

