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

"""Task sub_process."""

from typing import Dict

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSProcessDefinitionNotAssignException
from pydolphinscheduler.java_gateway import JavaGate


class SubProcess(Task):
    """Task SubProcess object, declare behavior for SubProcess task to dolphinscheduler."""

    _task_custom_attr = {"process_definition_code"}

    def __init__(self, name: str, process_definition_name: str, *args, **kwargs):
        super().__init__(name, TaskType.SUB_PROCESS, *args, **kwargs)
        self.process_definition_name = process_definition_name

    @property
    def process_definition_code(self) -> str:
        """Get process definition code, a wrapper for :func:`get_process_definition_info`."""
        return self.get_process_definition_info(self.process_definition_name).get(
            "code"
        )

    def get_process_definition_info(self, process_definition_name: str) -> Dict:
        """Get process definition info from java gateway, contains process definition id, name, code."""
        if not self.process_definition:
            raise PyDSProcessDefinitionNotAssignException(
                "ProcessDefinition must be provider for task SubProcess."
            )
        return JavaGate().get_process_definition_info(
            self.process_definition.user.name,
            self.process_definition.project.name,
            process_definition_name,
        )
