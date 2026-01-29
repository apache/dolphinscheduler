# 参与社区 review

贡献 DolphinScheduler 的方式，除了向 [团队](https://dolphinscheduler.apache.org/zh-cn/community) 中提到的 GitHub 仓库提交 Issues 和 pull requests 外，另一非常重要的方式是
review 社区的 Issues 或者 Pull Requests。通过别人 Issues 和 Pull Requests，你不仅能知道社区的最新进展和发展方向，还能了解别人代码的设
计思想，同时可以增加自己在社区的曝光、积累自己在社区的荣誉值。

任何人都被鼓励去 review 社区的 Issues 和 Pull Requests。我们还曾经发起过一个 Help Wanted 的邮件讨论，向社区征求贡献者协助 review Issues
以及 Pull Requests，详见 [邮件][mail-review-wanted]，并将其结果放到了 [GitHub Discussion][discussion-result-review-wanted] 中。

> 注意: 这里并不是说只有 [GitHub Discussion][discussion-result-review-wanted] 中提及的用户才可以协助 review Issue 或者 Pull Requests，
> 请记住社区的主张是 **任何人都被鼓励去 review 社区的 Issues 和 Pull Requests**。只是那部分用户在邮件列表意见征集的时候，表达了愿意付
> 出更多的时间，参与社区的 review。另一个好处是，当社区有不确定的问题的时，除了可以找 [团队](https://dolphinscheduler.apache.org/zh-cn/community) 中对应的 Members 外，还可以找
> [GitHub Discussion][discussion-result-review-wanted] 中提及的人解答对应的问题。如果你要想要加入到 [GitHub Discussion][discussion-result-review-wanted]
> 中，请在该 discussion 中评论并留下你感兴趣的模块，维护者会将你加入到对应的名单中。

## 怎么参与社区 review

DolphinScheduler 主要通过 GitHub 接收社区的贡献，其所有的 Issues 和 Pull Requests 都托管在 GitHub 中，如果你想参与 Issues 的 review
具体请查看 [review Issues](#issues) 章节，如果你是想要参与 Pull Requests 的 review 具体请查看 [review Pull Requests](#pull-requests)
章节。

### Issues

Review Issues 是指在 GitHub 中参与 [Issues][all-issues] 的讨论，并在对应的 Issues 给出建议。给出的建议包括但不限于如下的情况

|   情况    |          原因          |                        需增加标签                         |              需要的动作              |
|---------|----------------------|------------------------------------------------------|---------------------------------|
| 不需要修改   | 问题在 dev 分支最新代码中已经修复了 | [wontfix][label-wontfix]                             | 关闭 Issue，告知提出者将在那个版本发布，如已发布告知版本 |
| 重复的问题   | 之前已经存在相同的问题          | [duplicate][label-duplicate]                         | 关闭 Issue，告知提出者相同问题的连接           |
| 问题描述不清晰 | 没有明确说明问题如何复现         | [need more information][label-need-more-information] | 提醒用户需要增加缺失的描述                   |

除了个 issue 建议之外，给 Issue 分类也是非常重要的一个工作。分类后的 Issue 可以更好的被检索，为以后进一步处理提供便利。一个 Issue 可以被打上多个标签，常见的 Issue 分类有

|                    标签                    |        标签代表的情况        |
|------------------------------------------|-----------------------|
| [UI][label-ui]                           | UI 以及前端相关的 Issue      |
| [security][label-security]               | 安全相关的 Issue           |
| [user experience][label-user-experience] | 用户体验相关的 Issue         |
| [development][label-development]         | 开发者相关的 Issue          |
| [Python][label-python]                   | Python 相关的 Issue      |
| [plug-in][label-plug-in]                 | 插件相关的 Issue           |
| [document][label-document]               | 文档相关的 Issue           |
| [docker][label-docker]                   | docker 相关的 Issue      |
| [need verify][label-need-verify]         | Issue 需要被验证           |
| [e2e][label-e2e]                         | e2e 相关的 Issue         |
| [win-os][label-win-os]                   | windows 操作系统相关的 Issue |
| [suggestion][label-suggestion]           | Issue 为项目提出了建议        |

标签除了分类之外，还能区分 Issue 的优先级，优先级越高的标签越重要，越容易被重视，并会尽快被修复或者实现，优先级的标签如下

|                    标签                    | 优先级  |
|------------------------------------------|------|
| [priority:high][label-priority-high]     | 高优先级 |
| [priority:middle][label-priority-middle] | 中优先级 |
| [priority:low][label-priority-low]       | 低优先级 |

以上是常见的几个标签，更多的标签请查阅项目[全部的标签列表][label-all-list]

在阅读以下内容是，请确保你已经为 Issue 打了标签。

- 回复后及时去掉标签[Waiting for reply][label-waiting-for-reply]：在 [创建 Issue 的时候][issue-choose]，我们会为 Issue 打上特定的标签
  [Waiting for reply][label-waiting-for-reply]，方便定位还没有被回复的 Issue，所以当你 review 了 Issue 之后，就需要将标签
  [Waiting for reply][label-waiting-for-reply] 及时的从 Issue 中删除。
- 打上 [Waiting for review][label-waiting-for-review] 标当你不确定这个 Issue 是否被解决：当你查阅了 Issue 后，会有两个情况出现。一是
  问题已经被定位或解决，如果创建 Pull Requests 的话，则参考 [创建 PR](./submit-code.md)。二是你也不确定这个问题是否真的是
  被解决，这时你可以为 Issue 打上 [Waiting for review][label-waiting-for-review] 标签，并在 Issue 中 `@` 对应的人进行二次确认

当 Issue 需要被创建 Pull Requests 解决，也可以视情况打上部分标签

|                     标签                     |     标签代表的 PR     |
|--------------------------------------------|------------------|
| [Chore][label-chore]                       | 日常维护工作           |
| [Good first issue][label-good-first-issue] | 适合首次贡献者解决的 Issue |
| [easy to fix][label-easy-to-fix]           | 比较容易解决           |
| [help wanted][label-help-wanted]           | 向社区寻求帮忙          |

> 注意: 上面关于增加和删除标签的操作，目前只有成员可以操作，当你遇到需要增减标签的时候，但是不是成员是，可以 `@` 对应的成员让其帮忙增减。
> 但只要你有 GitHub 账号就能评论 Issue，并给出建议。我们鼓励社区每人都去评论并为 Issue 给出解答

### Pull Requests

<!-- markdown-link-check-disable -->

Review Pull 是指在 GitHub 中参与 [Pull Requests][all-prs] 的讨论，并在对应的 Pull Requests 给出建议。DolphinScheduler review
Pull Requests 与 [GitHub 的 reviewing changes in pull requests][gh-review-pr] 一样。你可以为 Pull Requests 提出自己的-->

- 当你认为这个 Pull Requests 没有问题，可以被合并的时候，可以根据 [GitHub 的 reviewing changes in pull requests][gh-review-pr] 的
  approve 流程同意这个 Pull Requests。
- 当你觉得这个 Pull Requests 需要被修改时，可以根据 [GitHub 的 reviewing changes in pull requests][gh-review-pr] 的 comment
  流程评论这个 Pull Requests。当你认为存在一定要先修复才能合并的问题，请参照 [GitHub 的 reviewing changes in pull requests][gh-review-pr]
  的 Request changes 流程要求贡献者修改 Pull Requests 的内容。

<!-- markdown-link-check-enable -->

为 Pull Requests 打上标签也是非常重要的一个环节，合理的分类能为后来的 reviewer 节省大量的时间。值得高兴的是，Pull Requests 的标签和 [Issues](#issues)
中提及的标签和用法是一致的，这能减少 reviewer 对标签的记忆。例如这个 Pull Requests 是和 docker 并且直接影响到用户部署的，我们可以为他
打上 [docker][label-docker] 和 [priority:high][label-priority-high] 的标签。

除了和 Issue 类似的标签外，Pull Requests 还有许多自己特有的标签

|                           标签                           |             含义              |
|--------------------------------------------------------|-----------------------------|
| [miss document][label-miss-document]                   | 该 Pull Requests 缺少文档 需要增加   |
| [first time contributor][label-first-time-contributor] | 该 Pull Requests 贡献者是第一次贡献项目 |
| [don't merge][label-do-not-merge]                      | 该 Pull Requests 有问题 暂时先不要合并 |

> 注意: 上面关于增加和删除标签的操作，目前只有成员可以操作，当你遇到需要增减标签的时候，可以 `@` 对应的成员让其帮忙增减。但只要你有 GitHub
> 账号就能评论 Pull Requests，并给出建议。我们鼓励社区每人都去评论并为 Pull Requests 给出建议

[mail-review-wanted]: https://lists.apache.org/thread/9flwlzrp69xjn6v8tdkbytq8glqp2k51
[discussion-result-review-wanted]: https://github.com/apache/dolphinscheduler/discussions/7545
[label-wontfix]: https://github.com/apache/dolphinscheduler/labels/wontfix
[label-duplicate]: https://github.com/apache/dolphinscheduler/labels/duplicate
[label-need-more-information]: https://github.com/apache/dolphinscheduler/labels/need%20more%20information
[label-win-os]: https://github.com/apache/dolphinscheduler/labels/win-os
[label-waiting-for-reply]: https://github.com/apache/dolphinscheduler/labels/Waiting%20for%20reply
[label-waiting-for-review]: https://github.com/apache/dolphinscheduler/labels/Waiting%20for%20review
[label-user-experience]: https://github.com/apache/dolphinscheduler/labels/user%20experience
[label-development]: https://github.com/apache/dolphinscheduler/labels/development
[label-ui]: https://github.com/apache/dolphinscheduler/labels/UI
[label-suggestion]: https://github.com/apache/dolphinscheduler/labels/suggestion
[label-security]: https://github.com/apache/dolphinscheduler/labels/security
[label-python]: https://github.com/apache/dolphinscheduler/labels/Python
[label-plug-in]: https://github.com/apache/dolphinscheduler/labels/plug-in
[label-document]: https://github.com/apache/dolphinscheduler/labels/document
[label-docker]: https://github.com/apache/dolphinscheduler/labels/docker
[label-all-list]: https://github.com/apache/dolphinscheduler/labels
[label-chore]: https://github.com/apache/dolphinscheduler/labels/Chore
[label-good-first-issue]: https://github.com/apache/dolphinscheduler/labels/good%20first%20issue
[label-help-wanted]: https://github.com/apache/dolphinscheduler/labels/help%20wanted
[label-easy-to-fix]: https://github.com/apache/dolphinscheduler/labels/easy%20to%20fix
[label-priority-high]: https://github.com/apache/dolphinscheduler/labels/priority%3Ahigh
[label-priority-middle]: https://github.com/apache/dolphinscheduler/labels/priority%3Amiddle
[label-priority-low]: https://github.com/apache/dolphinscheduler/labels/priority%3Alow
[label-miss-document]: https://github.com/apache/dolphinscheduler/labels/miss%20document
[label-first-time-contributor]: https://github.com/apache/dolphinscheduler/labels/first%20time%20contributor
[label-do-not-merge]: https://github.com/apache/dolphinscheduler/labels/don%27t%20merge
[label-e2e]: https://github.com/apache/dolphinscheduler/labels/e2e
[label-need-verify]: https://github.com/apache/dolphinscheduler/labels/need%20to%20verify
[issue-choose]: https://github.com/apache/dolphinscheduler/issues/new/choose
[all-issues]: https://github.com/apache/dolphinscheduler/issues
[all-prs]: https://github.com/apache/dolphinscheduler/pulls
[gh-review-pr]: https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/reviewing-changes-in-pull-requests/about-pull-request-reviews

