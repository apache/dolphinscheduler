# Pull Request Notice

## Preface

Pull Request is a way of software cooperation, which is a process of bringing code involving different functions into the trunk. During this process, the code can be discussed, reviewed, and modified.

In Pull Request, we try not to discuss the implementation of the code. The general implementation of the code and its logic should be determined in Issue. In the Pull Request, we only focus on the code format and code specification, so as to avoid wasting time caused by different opinions on implementation.

## Specification

### Pull Request Title

Title Format: [`Pull Request Type`-`Issue No`][`Module Name`] `Pull Request Description`

The corresponding relationship between `Pull Request Type` and `Issue Type` is as follows:

<table>
    <thead>
        <tr>
            <th style="width: 10%; text-align: center;">Issue Type</th>
            <th style="width: 20%; text-align: center;">Pull Request Type</th>
            <th style="width: 20%; text-align: center;">Example(Suppose Issue No is 3333)</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td style="text-align: center;">Feature</td>
            <td style="text-align: center;">Feature</td>
            <td style="text-align: center;">[Feature-3333][server] Implement xxx</td>
        </tr>
        <tr>
            <td style="text-align: center;">Bug</td>
            <td style="text-align: center;">Fix</td>
            <td style="text-align: center;">[Fix-3333][server] Fix xxx</td>
        </tr>
        <tr>
            <td style="text-align: center;">Improvement</td>
            <td style="text-align: center;">Improvement</td>
            <td style="text-align: center;">[Improvement-3333][alert] Improve the performance of xxx</td>
        </tr>
        <tr>
            <td style="text-align: center;">Test</td>
            <td style="text-align: center;">Test</td>
            <td style="text-align: center;">[Test-3333][api] Add the e2e test of xxx</td>
        </tr>
        <tr>
            <td style="text-align: center;">Sub-Task</td>
            <td style="text-align: center;">(Parent type corresponding to Sub-Task)</td>
            <td style="text-align: center;">[Feature-3333][server] Implement xxx</td>
        </tr>
    </tbody>
</table>

`Issue No` refers to the Issue number corresponding to the current Pull Request to be resolved, `Module Name` is the same as the `Module Name` of Issue.

### Pull Request Branch

Branch name format: `Pull Request type`-`Issue number`. e.g. Feature-3333

### Pull Request Content

Please refer to the commit message section.

### Pull Request Code Style

[//]: # (TODO: use the commented anchor below once our website template supports this syntax)
[//]: # (DolphinScheduler uses `Spotless` to automatically fix code style and formatting errors,)
[//]: # (see [Development Environment Setup]&#40;../development-environment-setup.md#code-style&#41; `Code Style` section for details.)

DolphinScheduler uses `Spotless` to automatically fix code style and formatting errors,
see [Development Environment Setup](../development-environment-setup.md) `Code Style` section for details.

### Question

- How to deal with one Pull Request to many Issues scenario.

  First of all, there are fewer scenarios for one Pull Request to many Issues.
  The root cause is that multiple issues need to do the same thing.
  Usually, there are two solutions to this scenario: the first is to merge multiple issues with into the same issue, and then close the other issues;
  the second is multiple issues have subtle differences.
  In this scenario, the responsibilities of each issue can be clearly divided. The type of each issue is marked as Sub-Task, and then these sub task type issues are associated with one issue.
  And each Pull Request is submitted should be associated with only one issue of a sub task.

