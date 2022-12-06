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

"""Test Task Pytorch."""
from copy import deepcopy
from unittest.mock import patch

import pytest

from pydolphinscheduler.tasks.pytorch import DEFAULT, Pytorch
from tests.testing.task import Task

CODE = 123
VERSION = 1

EXPECT = {
    "code": CODE,
    "version": VERSION,
    "description": None,
    "delayTime": 0,
    "taskType": "PYTORCH",
    "taskParams": {
        "resourceList": [],
        "localParams": [],
        "dependence": {},
        "conditionResult": {"successNode": [""], "failedNode": [""]},
        "waitStartTimeout": {},
    },
    "flag": "YES",
    "taskPriority": "MEDIUM",
    "workerGroup": "default",
    "environmentCode": None,
    "failRetryTimes": 0,
    "failRetryInterval": 1,
    "timeoutFlag": "CLOSE",
    "timeoutNotifyStrategy": None,
    "timeout": 0,
}


def test_pytorch_get_define():
    """Test task pytorch function get_define."""
    name = "task_conda_env"
    script = "main.py"
    script_params = "--dry-run --no-cuda"
    project_path = "https://github.com/pytorch/examples#mnist"
    is_create_environment = True
    python_env_tool = "conda"
    requirements = "requirements.txt"
    conda_python_version = "3.7"

    expect = deepcopy(EXPECT)
    expect["name"] = name
    task_params = expect["taskParams"]

    task_params["script"] = script
    task_params["scriptParams"] = script_params
    task_params["pythonPath"] = project_path
    task_params["otherParams"] = True
    task_params["isCreateEnvironment"] = is_create_environment
    task_params["pythonCommand"] = "${PYTHON_HOME}"
    task_params["pythonEnvTool"] = python_env_tool
    task_params["requirements"] = requirements
    task_params["condaPythonVersion"] = conda_python_version

    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(CODE, VERSION),
    ):
        task = Pytorch(
            name=name,
            script=script,
            script_params=script_params,
            project_path=project_path,
            is_create_environment=is_create_environment,
            python_env_tool=python_env_tool,
            requirements=requirements,
        )
        assert task.get_define() == expect


@pytest.mark.parametrize(
    "is_create_environment, project_path, python_command, expect",
    [
        (
            DEFAULT.is_create_environment,
            DEFAULT.project_path,
            DEFAULT.python_command,
            False,
        ),
        (True, DEFAULT.project_path, DEFAULT.python_command, True),
        (DEFAULT.is_create_environment, "/home", DEFAULT.python_command, True),
        (DEFAULT.is_create_environment, DEFAULT.project_path, "/usr/bin/python", True),
    ],
)
def test_other_params(is_create_environment, project_path, python_command, expect):
    """Test task pytorch function other_params."""
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        side_effect=Task("test_func_wrap", "func_wrap").gen_code_and_version,
    ):
        task = Pytorch(
            name="test",
            script="",
            script_params="",
            project_path=project_path,
            is_create_environment=is_create_environment,
            python_command=python_command,
        )
        assert task.other_params == expect
