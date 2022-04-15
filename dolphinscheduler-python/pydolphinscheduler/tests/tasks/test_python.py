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

from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.tasks.python import Python


def foo():  # noqa: D103
    print("hello world.")


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {"definition": "print(1)"},
            {
                "rawScript": "print(1)",
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "waitStartTimeout": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
            },
        ),
        (
            {"definition": "def foo():\n    print('I am foo')"},
            {
                "rawScript": "def foo():\n    print('I am foo')\nfoo()",
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "waitStartTimeout": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
            },
        ),
        (
            {"definition": foo},
            {
                "rawScript": 'def foo():  # noqa: D103\n    print("hello world.")\nfoo()',
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "waitStartTimeout": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
            },
        ),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_property_task_params(mock_code_version, attr, expect):
    """Test task python property."""
    task = Python("test-python-task-params", **attr)
    assert expect == task.task_params


@pytest.mark.parametrize(
    "script_code",
    [
        123,
        ("print", "hello world"),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_python_task_not_support_code(mock_code, script_code):
    """Test python task parameters."""
    name = "not_support_code_type"
    with pytest.raises(
        PyDSParamException, match="Parameter definition do not support .*?"
    ):
        task = Python(name, script_code)
        task.raw_script


@pytest.mark.parametrize(
    "name, script_code, raw",
    [
        ("string_define", 'print("hello world.")', 'print("hello world.")'),
        (
            "function_define",
            foo,
            'def foo():  # noqa: D103\n    print("hello world.")\nfoo()',
        ),
    ],
)
def test_python_get_define(name, script_code, raw):
    """Test task python function get_define."""
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
        assert shell.get_define() == expect
