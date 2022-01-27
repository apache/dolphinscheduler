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

[![GitHub Build][ga-py-test]][ga]
[![Code style: black][black-shield]][black-gh]
[![Imports: isort][isort-shield]][isort-gh]

**PyDolphinScheduler** is python API for Apache DolphinScheduler, which allow you definition
your workflow by python code, aka workflow-as-codes.

## Quick Start

### Installation

```shell
# Install
$ pip install apache-dolphinscheduler

# Check installation, it is success if you see version output, here we use 0.1.0 as example
$ python -c "import pydolphinscheduler; print(pydolphinscheduler.__version__)"
0.1.0
```

Here we show you how to install and run a simple example of pydolphinscheduler

### Start Server And Run Example

Before you run an example, you have to start backend server. You could follow [development setup][dev-setup]
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

After command execute, you could see a new project with single process definition named *tutorial* in the [UI][ui-project].

Until now, we finish quick start by an example of pydolphinscheduler and run it. If you want to inspect or join
pydolphinscheduler develop, you could take a look at [develop](./DEVELOP.md)

## What's more

For more detail information, please go to see **PyDolphinScheduler** [document][pyds-doc-home]

<!-- content -->
[pypi]: https://pypi.org/
[dev-setup]: https://dolphinscheduler.apache.org/en-us/development/development-environment-setup.html
[ui-project]: http://8.142.34.29:12345/dolphinscheduler/ui/#/projects/list
[pyds-doc-home]: https://dolphinscheduler.apache.org/python/index.html
<!-- badge -->
[ga-py-test]: https://github.com/apache/dolphinscheduler/actions/workflows/py-ci.yml/badge.svg?branch=dev
[ga]: https://github.com/apache/dolphinscheduler/actions
[black-shield]: https://img.shields.io/badge/code%20style-black-000000.svg
[black-gh]: https://github.com/psf/black
[isort-shield]: https://img.shields.io/badge/%20imports-isort-%231674b1?style=flat&labelColor=ef8336
[isort-gh]: https://pycqa.github.io/isort/
