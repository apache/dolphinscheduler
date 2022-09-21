# Community Review

Beside submit Issues and pull requests to the GitHub repository mentioned in [team](/us-en/community/community.html), another important way to
contribute to DolphinScheduler is reviewing GitHub Issues or Pull Requests. You can not only know the latest new and
direction of the community, but also understand the good design in others during you reviewing. At the same time, you can
increase your exposure in the community and accumulate your honor.

Anyone is encouraged to review Issues and Pull Requests. We also raise a Help Wanted email discussion to solicit contributors
from the community to review them. You could see detail in [mail][mail-review-wanted], we put the results of mail thread
in [GitHub Discussion][discussion-result-review-wanted].

> Note: It is only users mentioned in the [GitHub Discussion][discussion-result-review-wanted] can review Issues or Pull
> Requests, Community advocates **Anyone is encouraged to review Issues and Pull Requests**. Users in
> [GitHub Discussion][discussion-result-review-wanted] show their willing to review when we collect in the mail thread.
> The advantage of this list is when the community has discussion, in addition to the mention Members in [team](/us-en/community/community.html),
> you can also find some help in [GitHub Discussion][discussion-result-review-wanted] people. If you want to join the
> [GitHub Discussion][discussion-result-review-wanted], please comment in that discussion and leave a module you are interested
> in, and the maintainer will add you to the list.

## How Reviewing

DolphinScheduler receives community contributions through GitHub, and all its Issues and Pull Requests are hosted in GitHub.
If you want to join community by reviewing, please go to section [review Issues](#issues), if you prefer Pull Requests please
go to section [review Pull Requests](#pull-requests).

### Issues

Review Issues means discuss [Issues][all-issues] in GitHub and give suggestions on it. Include but are not limited to the following situations

|        Situation        |            Reason             |                        Label                         |                               Action                                |
|-------------------------|-------------------------------|------------------------------------------------------|---------------------------------------------------------------------|
| wont fix                | Has been fixed in dev branch  | [wontfix][label-wontfix]                             | Close Issue, inform creator the fixed version if it already release |
| duplicate issue         | Had the same problem before   | [duplicate][label-duplicate]                         | Close issue, inform creator the link of same issue                  |
| Description not clearly | Without detail reproduce step | [need more information][label-need-more-information] | Inform creator add more description                                 |

In addition give suggestion, add label for issue is also important during review. The labeled issues can be retrieved
better, which convenient for further processing. An issue can with more than one label. Common issue categories are:

|                  Label                   |            Meaning             |
|------------------------------------------|--------------------------------|
| [UI][label-UI]                           | UI and front-end related       |
| [security][label-security]               | Security Issue                 |
| [user experience][label-user-experience] | User experience Issue          |
| [development][label-development]         | Development Issue              |
| [Python][label-Python]                   | Python Issue                   |
| [plug-in][label-plug-in]                 | Plug-in Issue                  |
| [document][label-document]               | Document Issue                 |
| [docker][label-docker]                   | Docker Issue                   |
| [need verify][label-need-verify]         | Need verify Issue              |
| [e2e][label-e2e]                         | E2E Issue                      |
| [win-os][label-win-os]                   | windows operating system Issue |
| [suggestion][label-suggestion]           | Give suggestion to us          |

Beside classification, label could also set the priority of Issues. The higher the priority, the more attention pay
in the community, the easier it is to be fixed or implemented. The priority label are as follows

|                  Label                   |    priority     |
|------------------------------------------|-----------------|
| [priority:high][label-priority-high]     | High priority   |
| [priority:middle][label-priority-middle] | Middle priority |
| [priority:low][label-priority-low]       | Low priority    |

All the labels above in common label. For all labels in this project you could see in [full label list][label-all-list]

Before reading following content, please make sure you have labeled the Issue.

* Remove label [Waiting for reply][label-waiting-for-reply] after replying: Label [Waiting for reply][label-waiting-for-reply]
  added when [creating an Issue][issue-choose]. It makes positioning un reply issue more convenient, and you should remove
  this label after you reviewed it. If you do not remove it, will cause others to waste time looking on the same issue.
* Mark [Waiting for review][label-waiting-for-review] when not sure whether issue is resolved or not: There are two situations
  when you review issue. One is the problem has been located or resolved, maybe have to [Create PR](./submit-code.md)
  when necessary. Secondly, you are not sure about this issue, you can labeled [Waiting for review][label-waiting-for-review]
  and mention others to make a second confirmation.

When an Issue need to create Pull Requests, you could also labeled it from below.

|                   Label                    |                    Mean                     |
|--------------------------------------------|---------------------------------------------|
| [Chore][label-Chore]                       | Chore for project                           |
| [Good first issue][label-good-first-issue] | Good first issue for new contributor        |
| [easy to fix][label-easy-to-fix]           | Easy to fix, harder than `Good first issue` |
| [help wanted][label-help-wanted]           | Help wanted                                 |

> Note: Only members have permission to add or delete label. When you need to add or remove lebals but are not member,
> you can `@`  members to do that. But as long as you have a GitHub account, you can comment on issues and give suggestions.
> We encourage everyone in the community to comment and answer issues

### Pull Requests

<!-- markdown-link-check-disable -->
Review Pull mean discussing in [Pull Requests][all-PRs] in GitHub and giving suggestions to it. DolphinScheduler's 
Pull Requests reviewing are the same as [GitHub's reviewing changes in pull requests][gh-review-pr]. You can give your
suggestions in Pull Reque-->
* When you think the Pull Request is OK to be merged, you can agree to the Pull Request according to the "Approve" process
  in [GitHub's reviewing changes in pull requests][gh-review-pr].
* When you think Pull Request needs to be changed, you can comment it according to the "Comment" process in
  [GitHub's reviewing changes in pull requests][gh-review-pr]. And when you think issues that must be fixed before they
  merged, please follow "Request changes" in [GitHub's reviewing changes in pull requests][gh-review-pr] to ask contributors
  modify it.

<!-- markdown-link-check-enable -->

Labeled Pull Requests is an important part. Reasonable classification can save a lot of time for reviewers. The good news
is that the label's name and usage of Pull Requests are the same in [Issues](#issues), which can reduce the memory. For
example, if there is a Pull Request is related to docker and block deployment. We can label it with [docker][label-docker]
and [priority:high][label-priority-high].

Pull Requests have some unique labels of it own

|                         Label                          |                           Mean                           |
|--------------------------------------------------------|----------------------------------------------------------|
| [miss document][label-miss-document]                   | Pull Requests miss document, and should be add           |
| [first time contributor][label-first-time-contributor] | Pull Requests submit by first time contributor           |
| [don't merge][label-do-not-merge]                      | Pull Requests have some problem and should not be merged |

> Note: Only members have permission to add or delete label. When you need to add or remove lebals but are not member,
> you can `@`  members to do that. But as long as you have a GitHub account, you can comment on Pull Requests and give suggestions.
> We encourage everyone in the community to review Pull Requests

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
[label-UI]: https://github.com/apache/dolphinscheduler/labels/UI
[label-suggestion]: https://github.com/apache/dolphinscheduler/labels/suggestion
[label-security]: https://github.com/apache/dolphinscheduler/labels/security
[label-Python]: https://github.com/apache/dolphinscheduler/labels/Python
[label-plug-in]: https://github.com/apache/dolphinscheduler/labels/plug-in
[label-document]: https://github.com/apache/dolphinscheduler/labels/document
[label-docker]: https://github.com/apache/dolphinscheduler/labels/docker
[label-all-list]: https://github.com/apache/dolphinscheduler/labels
[label-Chore]: https://github.com/apache/dolphinscheduler/labels/Chore
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
[all-PRs]: https://github.com/apache/dolphinscheduler/pulls
[gh-review-pr]: https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/reviewing-changes-in-pull-requests/about-pull-request-reviews

