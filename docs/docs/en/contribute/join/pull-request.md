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

Code style is the thing you have to consider when you submit pull request for DolphinScheduler. We using [Checkstyle](https://checkstyle.sourceforge.io), a development tool to help programmers write Java code that adheres to a coding standard, in CI to keep DolphinScheduler codebase in the same style. Your pull request could not be merged if your code style checker failed. You could format your code by *Checkstyle* in your local environment before you submit your pull request to check code style. The activation step as below:

1. Prepare Checkstyle configuration file: You could download it manually by [click here](https://github.com/apache/dolphinscheduler/blob/3.0.4/style/checkstyle.xml), but find it in DolphinScheduler repository would be a better way. You could find configuration file in the path `style/checkstyle.xml` after you clone repository from Github.

2. Download Checkstyle plugins in Intellij IDEA: Search plugin by keyword **CheckStyle-IDEA** or install in [this page](https://plugins.jetbrains.com/plugin/1065-checkstyle-idea). You could see [install plugin](https://www.jetbrains.com/help/idea/managing-plugins.html#install_plugin_from_repo) if you do not know how to install plugin in Intellij IDEA

3. Configure and activate Checkstyle and Intellij IDEA code-style: After completing the above steps, you could configure and activate it in your environment. You could find Checkstyle plugins in the path `Preferences -> Tool -> Checkstyle`. After that you could activate Checkstyles as screenshot show

<p align="center">
    <img src="../../../../img/contribute/join/pull-request/checkstyle-idea.png" alt="checkstyle idea configuration" />
</p>

For now your Checkstyle plugins are setup, it would show codes and files which out of style. We highly recommend you configure Intellij IDEA code-style for auto-formatting your code in Intellij IDEA, you could find this setting in `Preferences -> Editor -> Code Style -> Java` and then activate it as screenshot show

<p align="center">
    <img src="../../../../img/contribute/join/pull-request/code-style-idea.png" alt="code style idea configuration" />
</p>

1. Format your codebase in Intellij IDEA before submit your pull request: After you done above steps, you could using Intellij IDEA shortcut `Command + L`(for Mac) or `Ctrl+L`(for Windows) to format your code. The best time to format your code is before you commit your change to your local git repository.

### Question

- How to deal with one Pull Request to many Issues scenario.

  First of all, there are fewer scenarios for one Pull Request to many Issues.
  The root cause is that multiple issues need to do the same thing.
  Usually, there are two solutions to this scenario: the first is to merge multiple issues with into the same issue, and then close the other issues;
  the second is multiple issues have subtle differences.
  In this scenario, the responsibilities of each issue can be clearly divided. The type of each issue is marked as Sub-Task, and then these sub task type issues are associated with one issue.
  And each Pull Request is submitted should be associated with only one issue of a sub task.