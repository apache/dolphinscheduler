# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.

"""Test Task python."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.tasks.python import Python, PythonTaskParams


@pytest.mark.parametrize(
    "name, value",
    [
        ("local_params", "local_params"),
        ("resource_list", "resource_list"),
        ("dependence", "dependence"),
        ("wait_start_timeout", "wait_start_timeout"),
        ("condition_result", "condition_result"),
    ],
)
def test_python_task_params_attr_setter(name, value):
    """Test python task parameters."""
    command = 'print("hello world.")'
    python_task_params = PythonTaskParams(command)
    assert command == python_task_params.raw_script
    setattr(python_task_params, name, value)
    assert value == getattr(python_task_params, name)


@pytest.mark.parametrize(
    "script_code",
    [
        123,
        ("print", "hello world"),
    ],
)
def test_python_task_not_support_code(script_code):
    """Test python task parameters."""
    name = "not_support_code_type"
    code = 123
    version = 1
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(code, version),
    ):
        with pytest.raises(ValueError, match="Parameter code do not support .*?"):
            Python(name, script_code)


def foo():  # noqa: D103
    print("hello world.")


@pytest.mark.parametrize(
    "name, script_code, raw",
    [
        ("string_define", 'print("hello world.")', 'print("hello world.")'),
        (
            "function_define",
            foo,
            'def foo():  # noqa: D103\n    print("hello world.")\n',
        ),
    ],
)
def test_python_to_dict(name, script_code, raw):
    """Test task python function to_dict."""
    code = 123
    version = 1
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "PYTHON",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "rawScript": raw,
            "dependence": {},
            "conditionResult": {"successNode": [""], "failedNode": [""]},
            "waitStartTimeout": {},
        },
        "flag": "YES",
        "taskPriority": "MEDIUM",
        "workerGroup": "default",
        "failRetryTimes": 0,
        "failRetryInterval": 1,
        "timeoutFlag": "CLOSE",
        "timeoutNotifyStrategy": None,
        "timeout": 0,
    }
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(code, version),
    ):
        shell = Python(name, script_code)
        assert shell.to_dict() == expect
