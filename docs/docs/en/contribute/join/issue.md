# Issue Notice

## Preface

Issues function is used to track various Features, Bugs, Functions, etc. The project maintainer can organize the tasks to be completed through issues.

Issue is an important step in drawing out a feature or bug,
and the contents that can be discussed in an issue are not limited to the features, the causes of the existing bugs, the research on preliminary scheme, and the corresponding implementation design and code design.

And only when the Issue is approved, the corresponding Pull Request should be implemented.

If an issue corresponds to a large feature, it is recommended to divide it into multiple small issues according to the functional modules and other dimensions.

## Specification

### Issue title

Title Format: [`Issue Type`][`Module Name`] `Issue Description`

The `Issue Type` is as follows:

<table>
    <thead>
        <tr>
            <th style="width: 10%; text-align: center;">Issue Type</th>
            <th style="width: 20%; text-align: center;">Description</th>
            <th style="width: 20%; text-align: center;">Example</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td style="text-align: center;">Feature</td>
            <td style="text-align: center;">Include expected features and functions</td>
            <td style="text-align: center;">[Feature][api] Add xxx api in xxx controller</td>
        </tr>
        <tr>
            <td style="text-align: center;">Bug</td>
            <td style="text-align: center;">Bugs in the program</td>
            <td style="text-align: center;">[Bug][api] Throw exception when xxx</td>
        </tr>
        <tr>
            <td style="text-align: center;">Improvement</td>
            <td style="text-align: center;">Some improvements of the current program, not limited to code format, program performance, etc</td>
            <td style="text-align: center;">[Improvement][server] Improve xxx between Master and Worker</td>
        </tr>
        <tr>
            <td style="text-align: center;">Test</td>
            <td style="text-align: center;">Specifically for the test case</td>
            <td style="text-align: center;">[Test][server] Add xxx e2e test</td>
        </tr>
        <tr>
            <td style="text-align: center;">Sub-Task</td>
            <td style="text-align: center;">Those generally are subtasks of feature class. For large features, they can be divided into many small subtasks to complete one by one</td>
            <td style="text-align: center;">[Sub-Task][server] Implement xxx in xxx</td>
        </tr>
    </tbody>
</table>

The `Module Name` is as follows:

<table>
    <thead>
        <tr>
            <th style="width: 10%; text-align: center;">Module Name</th>
            <th style="width: 20%; text-align: center;">Description</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td style="text-align: center;">alert</td>
            <td style="text-align: center;">Alert module</td>
        </tr>
        <tr>
            <td style="text-align: center;">api</td>
            <td style="text-align: center;">Application program interface layer module</td>
        </tr>
        <tr>
            <td style="text-align: center;">service</td>
            <td style="text-align: center;">Application service layer module</td>
        </tr>
        <tr>
            <td style="text-align: center;">dao</td>
            <td style="text-align: center;">Application data access layer module</td>
        </tr>
        <tr>
            <td style="text-align: center;">plugin</td>
            <td style="text-align: center;">Plugin module</td>
        </tr>
        <tr>
            <td style="text-align: center;">remote</td>
            <td style="text-align: center;">Communication module</td>
        </tr>
        <tr>
            <td style="text-align: center;">server</td>
            <td style="text-align: center;">Server module</td>
        </tr>
        <tr>
            <td style="text-align: center;">ui</td>
            <td style="text-align: center;">Front end module</td>
        </tr>
        <tr>
            <td style="text-align: center;">docs-zh</td>
            <td style="text-align: center;">Chinese document module</td>
        </tr>
        <tr>
            <td style="text-align: center;">docs</td>
            <td style="text-align: center;">English document module</td>
        </tr>
        <tr>
            <td style="text-align: center;">...</td>
            <td style="text-align: center;">-</td>
        </tr>
    </tbody>
</table>

### Issue content template

https://github.com/apache/dolphinscheduler/tree/dev/.github/ISSUE_TEMPLATE

### Contributor

Except for some special cases, it is recommended to discuss under issue or mailing list to determine the design scheme or provide the design scheme,
as well as the code implementation design before completing the issue.

If there are many different solutions, it is suggested to make a decision through mailing list or voting under issue.
The issue can be implemented after final scheme and code implementation design being approved.
The main purpose of this is to avoid wasting time caused by different opinions on implementation design or reconstruction in the pull request review stage.

### Question

- How to deal with the user who raises an issue does not know the module corresponding to the issue.

  It is true that most users when raising issue do not know which module the issue belongs to.
  In fact, this is very common in many open source communities. In this case, the committer / contributor actually knows the module affected by the issue.
  If the issue is really valuable after being approved by committer and contributor, then the committer can modify the issue title according to the specific module involved in the issue,
  or leave a message to the user who raises the issue to modify it into the corresponding title.

