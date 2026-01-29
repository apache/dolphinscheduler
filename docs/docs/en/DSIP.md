# DSIP

DolphinScheduler Improvement Proposal(DSIP) introduce major improvements to the Apache DolphinScheduler codebase. It is
not for small incremental improvements, and the purpose of DSIP is to notice and inform community the finished or coming
big feature for Apache DolphinScheduler.

## What is considered as DSIP

- Any major new feature, major improvement, introduce or remove components
- Any major change of public interfaces, such as API endpoints, web ui huge change

When the change in doubt and any committer thinks it should be DSIP, it does.

We use GitHub Issue and Apache mail thread to record and hold DSIP, for more detail you could go to section
[current DSIPs](#current-dsips) and [past DSIPs](#past-dsips).

As a DSIP, it should:

- Have a mail thread title started with `[DISCUSS]` in [dev@dolphinscheduler.apache.org][mail-to-dev]
- Have a GitHub Issue labeled with `DSIP`, and including the mail thread link in the description.

### Current DSIPs

Current DSIPs including all DSIP still work-in-progress, you could see in [current DSIPs][current-DSIPs]

### Past DSIPs

Past DSIPs including all DSIP already done or retired for some reason, you could see in [past DSIPs][past-DSIPs]

## DSIP Process

### Create GitHub Issue

All DSIP should start with GitHub Issue

- If you pretty sure your issue is DSIP, you could click and choose "DSIP" in
  [GitHub Issue][github-issue-choose]
- If you not sure about your issue is DSIP or not, you could click and choose "Feature request" in
  [GitHub Issue][github-issue-choose]. DolphinScheduler maintainer team would add label `DSIP`, mention you in the
  issue and lead you to this document when they think it should be DSIP.

You should add special prefix `[DSIP-XXX]`, `XXX` stand for the id DSIP. It's auto increment, and you could find the next
integer in [All DSIPs][all-DSIPs] issues.

### Send Discuss Mail

After issue labeled with "DSIP", you should send an email to [dev@dolphinscheduler.apache.org][mail-to-dev].
Describe the purpose, and the draft design about your idea.

Here is the template for mail

- Title: `[DISCUSS][DSIP-XXX] <CHANGE-TO-YOUR-LOVELY-PROPOSAL-TITLE>`, change `XXX` to special integer you just change in
  [GitHub Issue](#create-github-issue), and also change proposal title.
- Content:

  ```text
  Hi community,

  <CHANGE-TO-YOUR-PROPOSAL-DETAIL>

  I already add a GitHub Issue for my proposal, which you could see in <CHANGE-TO-YOUR-GITHUB-ISSUE-LINK>.

  Looking forward any feedback for this thread.
  ```

After community discuss and all of them think it worth as DSIP, you could [work on it](#work-on-it-or-create-subtask-for-it).
But if community think it should not be DSIP or even this change should not be included to DolphinScheduler, maintainers
terminate mail thread and remove label "DSIP" for GitHub Issue, or even close issue if it should not change.

### Work On It, Or Create Subtask For It

When your proposal pass in the mail thread, you could make your hand dirty and start the work. You could submit related
pull requests in GitHub if change should in one single commit. What's more, if proposal is too huge in single commit, you
could create subtasks in GitHub Issue like [DSIP-1][DSIP-1], and separate into multiple commit.

### Close After It Done

When DSIP is finished and all related PR were merged, you should reply the mail thread you created in
[step two](#send-discuss-mail) to notice community the result of the DSIP. After that, this DSIP GitHub Issue would be
closed and transfer from [current DSIPs][current-DSIPs] to [past DSIPs][past-DSIPs], but you could still find it in [All DSIPs][all-DSIPs]

## An Example For DSIP

* [[DSIP-1][Feature][Parent] Add Python API for DolphinScheduler][DSIP-1]: Have multiple subtasks and Projects on it.

[all-DSIPs]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+label%3A%22DSIP%22+
[current-DSIPs]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+is%3Aopen+label%3A%22DSIP%22
[past-DSIPs]: https://github.com/apache/dolphinscheduler/issues?q=is%3Aissue+is%3Aclosed+label%3A%22DSIP%22+
[github-issue-choose]: https://github.com/apache/dolphinscheduler/issues/new/choose
[mail-to-dev]: mailto:dev@dolphinscheduler.apache.org
[DSIP-1]: https://github.com/apache/dolphinscheduler/issues/6407

