# Commit Message 须知

### 前言

一个好的 commit message 是能够帮助其他的开发者（或者未来的开发者）快速理解相关变更的上下文，同时也可以帮助项目管理人员确定该提交是否适合包含在发行版中。但当我们在查看了很多开源项目的 commit log 后，发现一个有趣的问题，一部分开发者，代码质量很不错，但是 commit message 记录却比较混乱，当其他贡献者或者学习者在查看代码的时候，并不能通过 commit log 很直观的了解
该提交前后变更的目的，正如 Peter Hutterer 所言：Re-establishing the context of a piece of code is wasteful. We can’t avoid it completely, so our efforts should go to reducing it as much as possible. Commit messages can do exactly that and as a result, a commit message shows whether a developer is a good collaborator. 因此，DolphinScheduler 结合其他社区以及 Apache 官方文档制定了该规约。

### Commit Message RIP

#### 1：明确修改内容

commit message 应该明确说明该提交解决了哪些问题（bug 修复、功能增强等），以便于用户开发者更好的跟踪问题，明确版本迭代过程中的优化情况。

#### 2：关联相应的Pull Request 或者Issue

当我们的改动较大的时候，commit message 最好能够关联 GitHub 上的相关 Issue 或者 Pull Request，这样，我们的开发者在查阅代码的时候能够通过关联信息较为迅速的了解改代码提交的上下文情景，如果当前 commit 针对某个 issue，那么可以在 Footer 部分关闭这个 issue。

#### 3：统一的格式

格式化后的 CommitMessage 能够帮助我们提供更多的历史信息，方便快速浏览，同时也可以直接从 commit 生成 Change Log。

Commit message 应该包括三个部分：Header，Body 和 Footer。其中，Header 是必需的，Body 和 Footer 可以省略。

##### header

Header 部分只有一行，包括三个字段：type（必需）、scope（可选）和 subject（必需）。

[DS-ISSUE编号][type] subject

(1) type 用于说明 commit 的类别，只允许使用下面7个标识。

* feat：新功能（feature）
* fix：修补bug
* docs：文档（documentation）
* style： 格式（不影响代码运行的变动）
* refactor：重构（即不是新增功能，也不是修改bug的代码变动）
* test：增加测试
* chore：构建过程或辅助工具的变动

如果 type 为 feat 和 fix，则该 commit 将肯定出现在 Change log 之中。其他情况（docs、chore、style、refactor、test）建议不放入。

(2）scope

scope 用于说明 commit 影响的范围，比如 server、remote 等，如果没有更合适的范围，你可以用 *。

(3) subject

subject 是 commit 目的的简短描述，不超过50个字符。

##### Body

Body 部分是对本次 commit 的详细描述，可以分成多行，换行符将以72个字符换行，避免自动换行影响美观。

Body 部分需要注意以下几点：

* 使用动宾结构，注意使用现在时，比如使用 change 而非 changed 或 changes

* 首字母不要大写

* 语句最后不需要 ‘.’ (句号) 结尾

##### Footer

Footer只适用于两种情况

(1) 不兼容变动

如果当前代码与上一个版本不兼容，则 Footer 部分以 BREAKING CHANGE 开头，后面是对变动的描述、以及变动理由和迁移方法。

(2) 关闭 Issue

如果当前 commit 针对某个issue，那么可以在 Footer 部分关闭这个 issue,也可以一次关闭多个 issue 。

##### 举个例子

[DS-001][docs-zh] add commit message

* commit message RIP
* build some conventions
* help the commit messages become clean and tidy
* help developers and release managers better track issues
  and clarify the optimization in the version iteration

This closes #001

### 参考文档

[提交消息格式](https://cwiki.apache.org/confluence/display/GEODE/Commit+Message+Format)

[On commit messages-Peter Hutterer](http://who-t.blogspot.com/2009/12/on-commit-messages.html)

[RocketMQ Community Operation Conventions](https://mp.weixin.qq.com/s/LKM4IXAY-7dKhTzGu5-oug)
