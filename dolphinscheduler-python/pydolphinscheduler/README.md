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

# pydolphinscheduler

[![PyPi Version](https://img.shields.io/pypi/v/apache-dolphinscheduler.svg?style=flat-square&logo=PyPi)](https://pypi.org/project/apache-dolphinscheduler/)
[![PyPi Python Versions](https://img.shields.io/pypi/pyversions/apache-dolphinscheduler.svg?style=flat-square&logo=python)](https://pypi.org/project/apache-dolphinscheduler/)
[![PyPi License](https://img.shields.io/pypi/l/apache-dolphinscheduler.svg?style=flat-square)](https://pypi.org/project/apache-dolphinscheduler/)
[![PyPi Status](https://img.shields.io/pypi/status/apache-dolphinscheduler.svg?style=flat-square)](https://pypi.org/project/apache-dolphinscheduler/)
[![PyPi Downloads](https://img.shields.io/pypi/dm/apache-dolphinscheduler?style=flat-square)](https://pypi.org/project/apache-dolphinscheduler/)

[![Code style: black](https://img.shields.io/badge/code%20style-black-000000.svg?style=flat-square)](https://github.com/psf/black)
[![Imports: isort](https://img.shields.io/badge/%20imports-isort-%231674b1?style=flat-square&labelColor=ef8336)](https://pycqa.github.io/isort)
[![GitHub Build](https://github.com/apache/dolphinscheduler/actions/workflows/py-ci.yml/badge.svg?branch=dev)](https://github.com/apache/dolphinscheduler/actions?query=workflow%3A%22Python+API%22)

**PyDolphinScheduler** is python API for Apache DolphinScheduler, which allow you definition
your workflow by python code, aka workflow-as-codes.

## Quick Start

### Installation

```shell
# Install
python -m pip install apache-dolphinscheduler

# Verify installation is successful, it will show the version of apache-dolphinscheduler, here we use 0.1.0 as example
pydolphinscheduler version
# 0.1.0
```

> NOTE: package apache-dolphinscheduler not work on above Python version 3.10(including itself) in Window operating system
> due to dependence [py4j](https://pypi.org/project/py4j/) not work on those environments.

Here we show you how to install and run a simple example of pydolphinscheduler

### Start Server And Run Example

Before you run an example, you have to start backend server. You could follow
[development setup](../../docs/docs/en/contribute/development-environment-setup.md)
section "DolphinScheduler Standalone Quick Start" to set up developer environment. You have to start backend
and frontend server in this step, which mean that you could view DolphinScheduler UI in your browser with URL
http://localhost:12345/dolphinscheduler

After backend server is being start, all requests from `pydolphinscheduler` would be sent to backend server.
And for now we could run a simple example by:

<!-- TODO Add examples directory to dist package later. -->

```shell
# Please make sure your terminal could 
curl https://raw.githubusercontent.com/apache/dolphinscheduler/dev/dolphinscheduler-python/pydolphinscheduler/examples/tutorial.py -o ./tutorial.py
python ./tutorial.py
```

> **_NOTICE:_** Since Apache DolphinScheduler's tenant is requests while running command, you might need to change
> tenant value in `example/tutorial.py`. For now the value is `tenant_exists`, please change it to username exists
> in you environment.

After command execute, you could see a new project with single process definition named *tutorial* in the
[UI-project list](https://dolphinscheduler.apache.org/en-us/docs/latest/user_doc/guide/project/project-list.html).

## Develop

Until now, we finish quick start by an example of pydolphinscheduler and run it. If you want to inspect or join
pydolphinscheduler develop, you could take a look at [develop](./DEVELOP.md)

## Release

If you are interested in how to release **PyDolphinScheduler**, you could go and see at [release](./RELEASE.md)

## What's more

For more detail information, please go to see **PyDolphinScheduler** latest(unreleased) [document](https://dolphinscheduler.apache.org/python/dev/index.html)
