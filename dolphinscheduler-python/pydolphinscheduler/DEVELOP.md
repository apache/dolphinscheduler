<!--
 Licensed to the Apache Software Foundation (ASF) under one
 or more contributor license agreements.  See the NOTICE file
 distributed with this work for additional information
 regarding copyright ownership.  The ASF licenses this file
 to you under the Apache License, Version 2.0 (the
 "License"); you may not use this file except in compliance
 with the License.  You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing,
 software distributed under the License is distributed on an
 "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 KIND, either express or implied.  See the License for the
 specific language governing permissions and limitations
 under the License.
-->

# Develop

pydolphinscheduler is python API for Apache DolphinScheduler, it just defines what workflow look like instead of
store or execute it. We here use [py4j][py4j] to dynamically access Java Virtual Machine.

## Setup Develop Environment

**PyDolphinScheduler** use GitHub to hold all source code, you should clone the code before you do same change.

```shell
git clone git@github.com:apache/dolphinscheduler.git
```

Now, we should install all dependence to make sure we could run test or check code style locally

```shell
cd dolphinscheduler/dolphinscheduler-python/pydolphinscheduler
pip install .[dev]
```

Next, we have to open pydolphinscheduler project in you editor. We recommend you use [pycharm][pycharm]
instead of [IntelliJ IDEA][idea] to open it. And you could just open directory
`dolphinscheduler-python/pydolphinscheduler` instead of `dolphinscheduler-python`.


## Brief Concept

Apache DolphinScheduler is design to define workflow by UI, and pydolphinscheduler try to define it by code. When
define by code, user usually do not care user, tenant, or queue exists or not. All user care about is created
a new workflow by the code his/her definition. So we have some **side object** in `pydolphinscheduler/side`
directory, their only check object exists or not, and create them if not exists. 

### Process Definition

pydolphinscheduler workflow object name, process definition is also same name as Java object(maybe would be change to
other word for more simple).

### Tasks

pydolphinscheduler tasks object, we use tasks to define exact job we want DolphinScheduler do for us. For now,
we only support `shell` task to execute shell task. [This link][all-task] list all tasks support in DolphinScheduler
and would be implemented in the further.

## Code Style

We use [isort][isort] to automatically keep Python imports alphabetically, and use [Black][black] for code
formatter and [Flake8][flake8] for pep8 checker. If you use [pycharm][pycharm]or [IntelliJ IDEA][idea],
maybe you could follow [Black-integration][black-editor] to configure them in your environment.

Our Python API CI would automatically run code style checker and unittest when you submit pull request in
GitHub, you could also run static check locally.

```shell
# We recommend you run isort and Black before Flake8, because Black could auto fix some code style issue
# but Flake8 just hint when code style not match pep8

# Run Isort
isort .

# Run Black
black .

# Run Flake8
flake8
```

## Testing

pydolphinscheduler using [pytest][pytest] to test our codebase. GitHub Action will run our test when you create
pull request or commit to dev branch, with python version `3.6|3.7|3.8|3.9` and operating system `linux|macOS|windows`.

To test locally, you could directly run pytest after set `PYTHONPATH` 

```shell
PYTHONPATH=src/ pytest
```

We try to keep pydolphinscheduler usable through unit test coverage. 90% test coverage is our target, but for
now, we require test coverage up to 85%, and each pull request leas than 85% would fail our CI step
`Tests coverage`. We use [coverage][coverage] to check our test coverage, and you could check it locally by
run command.

```shell
coverage run && coverage report
```

It would not only run unit test but also show each file coverage which cover rate less than 100%, and `TOTAL`
line show you total coverage of you code. If your CI failed with coverage you could go and find some reason by
this command output.

<!-- content -->
[py4j]: https://www.py4j.org/index.html
[pycharm]: https://www.jetbrains.com/pycharm
[idea]: https://www.jetbrains.com/idea/
[all-task]: https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/shell.html
[pytest]: https://docs.pytest.org/en/latest/
[black]: https://black.readthedocs.io/en/stable/index.html
[flake8]: https://flake8.pycqa.org/en/latest/index.html
[black-editor]: https://black.readthedocs.io/en/stable/integrations/editors.html#pycharm-intellij-idea
[coverage]: https://coverage.readthedocs.io/en/stable/
[isort]: https://pycqa.github.io/isort/index.html
