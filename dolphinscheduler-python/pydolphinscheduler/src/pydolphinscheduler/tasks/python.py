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
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSParamException


class Python(Task):
    """Task Python object, declare behavior for Python task to dolphinscheduler."""

    _task_custom_attr = {
        "raw_script",
    }

    def __init__(self, name: str, code: Any, *args, **kwargs):
        super().__init__(name, TaskType.PYTHON, *args, **kwargs)
        self._code = code

    @property
    def raw_script(self) -> str:
        """Get python task define attribute `raw_script`."""
        if isinstance(self._code, str):
            return self._code
        elif isinstance(self._code, types.FunctionType):
            py_function = inspect.getsource(self._code)
            return py_function
        else:
            raise PyDSParamException(
                "Parameter code do not support % for now.", type(self._code)
            )
