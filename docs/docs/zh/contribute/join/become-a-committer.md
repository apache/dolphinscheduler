# 如何成为Apache Dolphinscheduler项目的committer

Dolphinscheduler 项目管理委员会（PMC）会评估候选人的贡献，并在合适的时间提名候选者。

像许多Apache项目一样，Dolphinscheduler欢迎所有形式的贡献，包括代码贡献、博客布道、新用户指南、公开演讲，以及以各种方式宣传项目和优化项目。

成为committer的第一步是要学习如何为Dolphinscheduler贡献并开始为Dolphinscheduler贡献，任何人可以向项目提交补丁、使用文档、测试用例等。

PMC会定期基于活跃贡献者们对Dolphinscheduler的贡献从他们中提名增加新的committer，要被提名为committer的条件如下：

1. 对Dolphinscheduler有持续的贡献：候选人应该对Dolphinscheduler有重大贡献，并且至少贡献了一个主要的组件，并在其中扮演了"owner"的角色。
2. 贡献的质量：候选人提交代码的质量应该比其他贡献者更高，此外他们应该表现出足够的专业知识来审查每个pr，包括确保它们符合Dolphinscheduler的工程实践（可测试性，文档，API稳定性，代码风格，等等）。委员会与committer对Dolphinscheduler的软件质量和可维护性共同负责。请注意，对Dolphinscheduler关键部分的贡献，比如它的核心模块，在评估质量时将会有更高的标准，这些领域的贡献者将面临更多的修改审查。
3. 社区参与：候选人在所有的社区互动中应该有一个建设性的和友好的态度，他们还应该在开发和用户列表中活跃，帮助指导新的贡献者和用户。在设计讨论中，候选人应该保持专业和开发的态度，即使面对分歧时也要有合理的沟通。

## 提名新的committer

在Dolphinscheduler中，committer提名只能由现有的PMC成员开始。如果一个新的提交者觉得他/她有资格，他/她应该联系任何现有的PMC成员并进行讨论。如果这一点在PMC的一些成员中得到了认同，那么这个过程就会启动。

建议采取以下步骤（仅需要现有2个PMC成员就可以发起）：
1. 发送一封标题为"[讨论]晋升xxx为新的committer"的电子邮件到`private@dolphinscheduler.apache.org`，并在邮件中列出该候选人的重要贡献，这样就可以提名流程。
2. 保持讨论要超过3天，但不超过1周，除非有任何明确的反对。
3. 如果PMC普遍同意该提议，请发送一封题为"[投票]提名xxx成为新的committer"的电子邮件到`private@dolphinscheduler.apache.org`
4. 保持投票过程超过3天，但不超过1周。至少3 + 1票，且无反对票，则视为"共识批准"。注：+1票 > -1票。
5. 发送一封题为"[结果] [投票]晋升xxx为新的committer"的电子邮件到`private@dolphinscheduler.apache.org`，并列出投票细节，包括谁是投票者。

## 邀请新的committer

发起提名的PMC成员负责向新的committer发出邀请，并指导他/她建立ASF精神思想。

PMC成员应使用以下模板向新的committer发送一封电子邮件:

```
To: <invitee name>@gmail.com
Cc: private@dolphinscheduler.apache.org
Subject: Invitation to become dolphinscheduler committer: <invitee name>

Hello <invitee name>,

The Dolphinscheduler Project Management Committee] (PMC) 
hereby offers you committer privileges to the project. These privileges are
offered on the understanding that you'll use them
reasonably and with common sense. We like to work on trust
rather than unnecessary constraints.

Being a committer enables you to more easily make 
changes without needing to go through the patch 
submission process. 

Being a committer does not require you to 
participate any more than you already do. It does 
tend to make one even more committed.  You will 
probably find that you spend more time here.

Of course, you can decline and instead remain as a 
contributor, participating as you do now.

A. This personal invitation is a chance for you to 
accept or decline in private.  Either way, please 
let us know in reply to the [private@dolphinscheduler.apache.org] 
address only.

B. If you accept, the next step is to register an iCLA:
    1. Details of the iCLA and the forms are found 
    through this link: http://www.apache.org/licenses/#clas

    2. Instructions for its completion and return to 
    the Secretary of the ASF are found at
    http://www.apache.org/licenses/#submitting

    3. When you transmit the completed iCLA, request 
    to notify the Apache Dolphinscheduler and choose a 
    unique Apache id. Look to see if your preferred 
    id is already taken at 
    http://people.apache.org/committer-index.html     
    This will allow the Secretary to notify the PMC 
    when your iCLA has been recorded.

When recording of your iCLA is noticed, you will 
receive a follow-up message with the next steps for 
establishing you as a committer.
```

## 接受邀请

新的提交者应该回复`private@dolphinscheduler.apache.org`（选择`回复所有`），并表达他/她接受邀请的意愿。

一旦接受了邀请，新的committer必须采取以下步骤：
1. 订阅`dev@dolphinscheduler.apache.org`，通常情况下，这已经完成
2. 选择一个不在[apache committers list page](http://people.apache.org/committer-index.html)上的Apache ID
3. 下载[ICLA](https://www.apache.org/licenses/icla.pdf)（如果新的提交者将项目作为日常工作来贡献，则应下载[CCLA](http://www.apache.org/licenses/cla-corporate.pdf)）
4. 在`icla.pdf`(或`ccla.pdf`)中填写正确的信息后，打印出来，手写签名，扫描成PDF格式，并作为附件发送至[secretary@apache.org](mailto:secretary@apache.org)。(如果希望使用电子签名，请按照[本页](http://www.apache.org/licenses/contributor-agreements.html#submitting)上的步骤进行)
5. PMC将等待Apache秘书确认所提交的ICLA（或CCLA），新的committer和PMC将收到以下电子邮件：

```
Dear XXX,

This message acknowledges receipt of your ICLA, which has been filed in the Apache Software Foundation records.

Your account has been requested for you and you should receive email with next steps
within the next few days (can take up to a week).

Please refer to https://www.apache.org/foundation/how-it-works.html#developers
for more information about roles at Apache.
```

在极端情况下，如果账户申请过程中出现问题，PMC成员应该联系项目的V.P.，V.P.可以通过[Apache Account Submission Helper Form](https://whimsy.apache.org/officers/acreq)进行申请。

几天后，新的committer将收到一封确认创建账户的电子邮件，标题为 `欢迎来到Apache软件基金会(ASF)!`。祝贺你! 现在有了一个正式的Apache ID。

PMC成员应通过[花名册](https://whimsy.apache.org/roster/pmc/dolphinscheduler)将新的提交者加入官方committer名单。

## 初始化Apache ID和设置开发环境

1. 进入[Apache Account Utility Platform](https://id.apache.org/)，创建你的密码，设置你的个人邮箱(`Forwarding email address`)和GitHub账户(`Your GitHub Username`)。此后不久（2小时内），我们将通过电子邮件向你发出组织邀请。
2. 如果你想使用`xxx@apache.org`电子邮件服务，请参考[这里](https://infra.apache.org/committer-email.html)。推荐使用`Gmail`，因为这种转发模式在大多数邮箱服务设置中都不容易被设置。
3. 根据`GitHub 2FA wiki`，在[Github](http://github.com/)上启用双因素授权（2FA）。当你将2FA设置为关闭时，它将被相应的Apache committer写权限组除名，直到你再次开启。(**注意：对待你的恢复码要像对待你的密码一样严谨！**)
4. 使用[GitBox Account Linking Utility](https://gitbox.apache.org/setup/)获得Dolphinscheduler项目的写入许可。

如果你想在Apache GitHub组织中公开显示，你需要到[Apache GitHub组织人员页面](https://github.com/orgs/apache/people)搜索你自己，并选择 "组织可见性 "为 "公开"。

## Committer的权利、义务和责任

Dolphinscheduler项目并不要求你在成为committer后继续做出贡献，但我们真心希望你能继续在社区中发挥榜样作用!

作为committer，你应该：
1. review贡献者的pr，并将合格的pr合并到项目主分支，一个pr通常包含多个提交，这些提交**必须被压缩并合并为一个提交**，**并附上解释说明**，我们建议新的committer在review完pr后再请求资深committer重新review pr。
2. 创建并推送代码至项目中的新分支。
3. 按照发布流程来准备一个新的版本，记住要与committer团队确认，发布版本之前与团队确认是必须要做的事情。

PMC希望新的committer能够参与发布过程以及发布投票，尽管他们的投票将被视为"+1无约束力"，熟悉发布过程是晋升为PMC成员的关键。

## 项目管理委员会

项目管理委员会（PMC）成员在代码贡献方面没有任何特殊权利，
他们只是监督项目，并确保其遵循Apache的要求，其职能包括：

1. 对版本发布和许可检查进行有约束力的投票
2. 认可新的committer和PMC成员
3. 确定品牌问题和品牌保护
4. 回应ASF董事会提出的问题，并采取必要的行动

副主席和PMC的主席是秘书，负责初始化董事会报告。

在大多数情况下，一个新的PMC成员是由committer团队提名的，但也有可能直接成为PMC成员，只要PMC同意提名并确信候选人有资格。例如，可以通过他/她曾经是Apache成员、Apache官员或其他项目的PMC成员来证明这一点。

新的PMC投票过程也应遵循 新的PMC投票过程也应遵循`[讨论]`、`[投票]`和`[结果][投票]`程序，使用一个私人邮件列表，就像新提交者的投票过程，在发送邀请之前，PMC还必须向Apache董事会发送NOTICE邮件；`[讨论]`、`[投票]`和`[结果][投票]`程序使用私人邮件列表，就像新committer的投票过程，在发送邀请之前，PMC还必须向Apache董事会发送一封NOTICE邮件：

```
To: board@apache.org
Cc: private@dolphinscheduler.apache.org
Subject: [NOTICE] Jane Doe for Dolphinscheduler PMC

Dolphinscheduler proposes to invite Jane Doe (janedoe) to join the PMC.

(include if a vote was held) The vote result is available here: https://lists.apache.org/...
```

72小时后，如果董事会不反对提名（大多数情况下不会反对），那么就可以向候选人发出邀请。

一旦候选人接受邀请，PMC成员应通过[花名册](https://whimsy.apache.org/roster/pmc/dolphinscheduler)将新成员加入PMC正式名单。
