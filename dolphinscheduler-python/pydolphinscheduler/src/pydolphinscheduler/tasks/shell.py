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

"""Task shell."""

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task, TaskParams


class ShellTaskParams(TaskParams):
    """Parameter only for shell task types."""

    def __init__(self, raw_script: str, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.raw_script = raw_script


class Shell(Task):
    """Task shell object, declare behavior for shell task to dolphinscheduler.

    TODO maybe we could use instance name to replace attribute `name`
    which is simplify as `task_shell = Shell(command = "echo 1")` and
    task.name assign to `task_shell`
    """

    def __init__(self, name: str, command: str, *args, **kwargs):
        task_params = ShellTaskParams(raw_script=command)
        super().__init__(name, TaskType.SHELL, task_params, *args, **kwargs)
