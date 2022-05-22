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

"""Task Flink."""

from typing import Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.engine import Engine, ProgramType


class FlinkVersion(str):
    """Flink version, for now it just contain `HIGHT` and `LOW`."""

    LOW_VERSION = "<1.10"
    HIGHT_VERSION = ">=1.10"


class DeployMode(str):
    """Flink deploy mode, for now it just contain `LOCAL` and `CLUSTER`."""

    LOCAL = "local"
    CLUSTER = "cluster"


class Flink(Engine):
    """Task flink object, declare behavior for flink task to dolphinscheduler."""

    _task_custom_attr = {
        "deploy_mode",
        "flink_version",
        "slot",
        "task_manager",
        "job_manager_memory",
        "task_manager_memory",
        "app_name",
        "parallelism",
        "main_args",
        "others",
    }

    def __init__(
        self,
        name: str,
        main_class: str,
        main_package: str,
        program_type: Optional[ProgramType] = ProgramType.SCALA,
        deploy_mode: Optional[DeployMode] = DeployMode.CLUSTER,
        flink_version: Optional[FlinkVersion] = FlinkVersion.LOW_VERSION,
        app_name: Optional[str] = None,
        job_manager_memory: Optional[str] = "1G",
        task_manager_memory: Optional[str] = "2G",
        slot: Optional[int] = 1,
        task_manager: Optional[int] = 2,
        parallelism: Optional[int] = 1,
        main_args: Optional[str] = None,
        others: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(
            name,
            TaskType.FLINK,
            main_class,
            main_package,
            program_type,
            *args,
            **kwargs
        )
        self.deploy_mode = deploy_mode
        self.flink_version = flink_version
        self.app_name = app_name
        self.job_manager_memory = job_manager_memory
        self.task_manager_memory = task_manager_memory
        self.slot = slot
        self.task_manager = task_manager
        self.parallelism = parallelism
        self.main_args = main_args
        self.others = others
