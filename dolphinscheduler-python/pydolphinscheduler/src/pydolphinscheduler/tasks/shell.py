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
from pydolphinscheduler.core.task import Task


class Shell(Task):
    """Task shell object, declare behavior for shell task to dolphinscheduler.

    :param name: A unique, meaningful string for the shell task.
    :param command: One or more command want to run in this task.

        It could be simply command::

            Shell(name=..., command="echo task shell")

        or maybe same commands trying to do complex task::

            command = '''echo task shell step 1;
            echo task shell step 2;
            echo task shell step 3
            '''

            Shell(name=..., command=command)

    """

    # TODO maybe we could use instance name to replace attribute `name`
    #  which is simplify as `task_shell = Shell(command = "echo 1")` and
    #  task.name assign to `task_shell`

    _task_custom_attr = {
        "raw_script",
    }

    ext: set = {".sh", ".zsh"}
    ext_attr: str = "_raw_script"

    def __init__(self, name: str, command: str, *args, **kwargs):
        self._raw_script = command
        super().__init__(name, TaskType.SHELL, *args, **kwargs)
