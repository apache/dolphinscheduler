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

"""Task Pytorch."""
from typing import Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task


class DEFAULT:
    """Default values for Pytorch."""

    is_create_environment = False
    project_path = "."
    python_command = "${PYTHON_HOME}"


class Pytorch(Task):
    """Task Pytorch object, declare behavior for Pytorch task to dolphinscheduler.

    See also: `DolphinScheduler Pytorch Task Plugin
    <https://dolphinscheduler.apache.org/en-us/docs/dev/user_doc/guide/task/pytorch.html>`_

    :param name: task name
    :param script: Entry to the Python script file that you want to run.
    :param script_params: Input parameters at run time.
    :param project_path: The path to the project. Default "." .
    :param is_create_environment: is create environment. Default False.
    :param python_command: The path to the python command. Default "${PYTHON_HOME}".
    :param python_env_tool: The python environment tool. Default "conda".
    :param requirements: The path to the requirements.txt file. Default "requirements.txt".
    :param conda_python_version: The python version of conda environment. Default "3.7".
    """

    _task_custom_attr = {
        "script",
        "script_params",
        "other_params",
        "python_path",
        "is_create_environment",
        "python_command",
        "python_env_tool",
        "requirements",
        "conda_python_version",
    }

    def __init__(
        self,
        name: str,
        script: str,
        script_params: str = "",
        project_path: Optional[str] = DEFAULT.project_path,
        is_create_environment: Optional[bool] = DEFAULT.is_create_environment,
        python_command: Optional[str] = DEFAULT.python_command,
        python_env_tool: Optional[str] = "conda",
        requirements: Optional[str] = "requirements.txt",
        conda_python_version: Optional[str] = "3.7",
        *args,
        **kwargs,
    ):
        """Init Pytorch task."""
        super().__init__(name, TaskType.PYTORCH, *args, **kwargs)
        self.script = script
        self.script_params = script_params
        self.is_create_environment = is_create_environment
        self.python_path = project_path
        self.python_command = python_command
        self.python_env_tool = python_env_tool
        self.requirements = requirements
        self.conda_python_version = conda_python_version

    @property
    def other_params(self):
        """Return other params."""
        conds = [
            self.is_create_environment != DEFAULT.is_create_environment,
            self.python_path != DEFAULT.project_path,
            self.python_command != DEFAULT.python_command,
        ]
        return any(conds)
