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
import logging
import re
import types
from typing import Union

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSParamException

log = logging.getLogger(__file__)


class Python(Task):
    """Task Python object, declare behavior for Python task to dolphinscheduler.

    Python task support two types of parameters for :param:``code``, and here is an example:

    Using str type of :param:``code``

    .. code-block:: python

        python_task = Python(name="str_type", code="print('Hello Python task.')")

    Or using Python callable type of :param:``code``

    .. code-block:: python

        def foo():
            print("Hello Python task.")

        python_task = Python(name="str_type", code=foo)

    :param name: The name for Python task. It define the task name.
    :param definition: String format of Python script you want to execute or Python callable you
        want to execute.
    """

    _task_custom_attr = {
        "raw_script",
    }

    def __init__(
        self, name: str, definition: Union[str, types.FunctionType], *args, **kwargs
    ):
        super().__init__(name, TaskType.PYTHON, *args, **kwargs)
        self.definition = definition

    def _build_exe_str(self) -> str:
        """Build executable string from given definition.

        Attribute ``self.definition`` almost is a function, we need to call this function after parsing it
        to string. The easier way to call a function is using syntax ``func()`` and we use it to call it too.
        """
        if isinstance(self.definition, types.FunctionType):
            py_function = inspect.getsource(self.definition)
            func_str = f"{py_function}{self.definition.__name__}()"
        else:
            pattern = re.compile("^def (\\w+)\\(")
            find = pattern.findall(self.definition)
            if not find:
                log.warning(
                    "Python definition is simple script instead of function, with value %s",
                    self.definition,
                )
                return self.definition
            # Keep function str and function callable always have one blank line
            func_str = (
                f"{self.definition}{find[0]}()"
                if self.definition.endswith("\n")
                else f"{self.definition}\n{find[0]}()"
            )
        return func_str

    @property
    def raw_script(self) -> str:
        """Get python task define attribute `raw_script`."""
        if isinstance(self.definition, (str, types.FunctionType)):
            return self._build_exe_str()
        else:
            raise PyDSParamException(
                "Parameter definition do not support % for now.", type(self.definition)
            )
