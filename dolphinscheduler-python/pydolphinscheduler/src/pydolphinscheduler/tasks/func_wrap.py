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

"""Task function wrapper allows using decorator to create a task."""

import functools
import inspect
import itertools
import types

from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.tasks.python import Python


def _get_func_str(func: types.FunctionType) -> str:
    """Get Python function string without indent from decorator.

    :param func: The function which wraps by decorator ``@task``.
    """
    lines = inspect.getsourcelines(func)[0]

    src_strip = ""
    lead_space_num = None
    for line in lines:
        if lead_space_num is None:
            lead_space_num = sum(1 for _ in itertools.takewhile(str.isspace, line))
        if line.strip() == "@task":
            continue
        elif line.strip().startswith("@"):
            raise PyDSParamException(
                "Do no support other decorators for function ``task`` decorator."
            )
        src_strip += line[lead_space_num:]
    return src_strip


def task(func: types.FunctionType):
    """Decorate which covert Python function into pydolphinscheduler task."""

    @functools.wraps(func)
    def wrapper(*args, **kwargs):
        func_str = _get_func_str(func)
        return Python(
            name=kwargs.get("name", func.__name__), definition=func_str, *args, **kwargs
        )

    return wrapper
