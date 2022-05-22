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

"""Mock class Task for other test."""

import uuid

from pydolphinscheduler.core.task import Task as SourceTask


class Task(SourceTask):
    """Mock class :class:`pydolphinscheduler.core.task.Task` for unittest."""

    DEFAULT_VERSION = 1

    def gen_code_and_version(self):
        """Mock java gateway code and version, convenience method for unittest."""
        return uuid.uuid1().time, self.DEFAULT_VERSION


class TaskWithCode(SourceTask):
    """Mock class :class:`pydolphinscheduler.core.task.Task` and it return some code and version."""

    def __init__(
        self, name: str, task_type: str, code: int, version: int, *args, **kwargs
    ):
        self._constant_code = code
        self._constant_version = version
        super().__init__(name, task_type, *args, **kwargs)

    def gen_code_and_version(self):
        """Mock java gateway code and version, convenience method for unittest."""
        return self._constant_code, self._constant_version
