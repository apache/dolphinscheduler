# Commit Message Notice

### Preface

A good commit message can help other developers (or future developers) quickly understand the context of related changes, and can also help project managers determine whether the commit is suitable for inclusion in the release. But when we checked the commit logs of many open source projects, we found an interesting problem. Some developers have very good code quality, but the commit message record is rather confusing. When other contributors or learners are viewing the code, it can’t be intuitively understood through commit log.
The purpose of the changes before and after the submission, as Peter Hutterer said：Re-establishing the context of a piece of code is wasteful. We can’t avoid it completely, so our efforts should go to reducing it as much as possible. Commit messages can do exactly that and as a result, a commit message shows whether a developer is a good collaborator. Therefore, DolphinScheduler developed the protocol in conjunction with other communities and official Apache documents.

### Commit Message RIP

#### 1：Clearly modify the content

A commit message should clearly state what issues (bug fixes, function enhancements, etc.) the submission solves, so that other developers can better track the issues and clarify the optimization during the version iteration process.

#### 2：Associate the corresponding Pull Request or Issue

When our changes are large, the commit message should best be associated with the relevant Issue or Pull Request on GitHub, so that our developers can quickly understand the context of the code submission through the associated information when reviewing the code. If the current commit is for an issue, then the issue can be closed in the Footer section.

#### 3：Unified format

The formatted CommitMessage can help provide more historical information for quick browsing, and it can also generate a Change Log directly from commit.

Commit message should include three parts: Header, Body and Footer. Among them, Header is required, Body and Footer can be omitted.

##### Header

The header part has only one line, including three fields: type (required), scope (optional), and subject (required).

[DS-ISSUE number][type] subject

(1) Type is used to indicate the category of commit, and only the following 7 types are allowed.

- feat：features
- fix：Bug fixes
- docs：Documentation
- style： Format (does not affect changes in code operation)
- refactor：Refactoring (It is not a new feature or a code change to fix a bug)
- test：Add test
- chore：Changes in the build process or auxiliary tools

If the type is feat and fix, the commit will definitely appear in the change log. Other types (docs, chore, style, refactor, test) are not recommended.

(2) Scope

Scope is used to indicate the scope of commit impact, such as server, remote, etc. If there is no suitable scope, you can use \*.

(3) subject

Subject is a short description of the purpose of the commit, no more than 50 characters.

##### Body

The body part is a detailed description of this commit, which can be divided into multiple lines, and the line break will wrap with 72 characters to avoid automatic line wrapping affecting the appearance.

Note the following points in the Body section:

- Use the verb-object structure, note the use of present tense. For example, use change instead of changed or changes

- Don't capitalize the first letter

- The end of the sentence does not need a ‘.’ (period)

##### Footer

Footer only works in two situations

(1) Incompatible changes

If the current code is not compatible with the previous version, the Footer part starts with BREAKING CHANGE, followed by a description of the change, the reason for the change, and the migration method.

(2) Close Issue

If the current commit is for a certain issue, you can close the issue in the Footer section, or close multiple issues at once.

##### For Example

```
[DS-001][docs-en] add commit message

- commit message RIP
- build some conventions
- help the commit messages become clean and tidy
- help developers and release managers better track issues
  and clarify the optimization in the version iteration

This closes #001
```

### Reference documents

[Commit message format](https://cwiki.apache.org/confluence/display/GEODE/Commit+Message+Format)

[On commit messages-Peter Hutterer](http://who-t.blogspot.com/2009/12/on-commit-messages.html)

[RocketMQ Community Operation Conventions](https://mp.weixin.qq.com/s/LKM4IXAY-7dKhTzGu5-oug)
