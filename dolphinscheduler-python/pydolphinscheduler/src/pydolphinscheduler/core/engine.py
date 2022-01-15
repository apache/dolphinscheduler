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

"""Module engine."""

from typing import Dict, Optional

from py4j.protocol import Py4JJavaError

from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.java_gateway import launch_gateway


class ProgramType(str):
    """Type of program engine runs, for now it just contain `JAVA`, `SCALA` and `PYTHON`."""

    JAVA = "JAVA"
    SCALA = "SCALA"
    PYTHON = "PYTHON"


class Engine(Task):
    """Task engine object, declare behavior for engine task to dolphinscheduler.

    This is the parent class of spark, flink and mr tasks,
    and is used to provide the programType, mainClass and mainJar task parameters for reuse.
    """

    def __init__(
        self,
        name: str,
        task_type: str,
        main_class: str,
        main_package: str,
        program_type: Optional[ProgramType] = ProgramType.SCALA,
        *args,
        **kwargs
    ):
        super().__init__(name, task_type, *args, **kwargs)
        self.main_class = main_class
        self.main_package = main_package
        self.program_type = program_type
        self._resource = {}

    def get_resource_info(self, program_type, main_package):
        """Get resource info from java gateway, contains resource id, name."""
        if self._resource:
            return self._resource
        else:
            gateway = launch_gateway()
            try:
                self._resource = gateway.entry_point.getResourcesFileInfo(
                    program_type, main_package
                )
            # Handler source do not exists error, for now we just terminate the process.
            except Py4JJavaError as ex:
                raise PyDSParamException(str(ex.java_exception))
            return self._resource

    def get_jar_id(self) -> int:
        """Get jar id from java gateway, a wrapper for :func:`get_resource_info`."""
        return self.get_resource_info(self.program_type, self.main_package).get("id")

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for engine children task.

        children task have some specials attribute for task_params, and is odd if we
        directly set as python property, so we Override Task.task_params here.
        """
        params = super().task_params
        custom_params = {
            "programType": self.program_type,
            "mainClass": self.main_class,
            "mainJar": {
                "id": self.get_jar_id(),
            },
        }
        params.update(custom_params)
        return params
