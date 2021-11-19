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

"""Task Python."""

import inspect
import types
from typing import Any

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task, TaskParams


class PythonTaskParams(TaskParams):
    """Parameter only for Python task types."""

    def __init__(self, raw_script: str, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.raw_script = raw_script


class Python(Task):
    """Task Python object, declare behavior for Python task to dolphinscheduler."""

    def __init__(self, name: str, code: Any, *args, **kwargs):
        if isinstance(code, str):
            task_params = PythonTaskParams(raw_script=code)
        elif isinstance(code, types.FunctionType):
            py_function = inspect.getsource(code)
            task_params = PythonTaskParams(raw_script=py_function)
        else:
            raise ValueError("Parameter code do not support % for now.", type(code))
        super().__init__(name, TaskType.PYTHON, task_params, *args, **kwargs)
