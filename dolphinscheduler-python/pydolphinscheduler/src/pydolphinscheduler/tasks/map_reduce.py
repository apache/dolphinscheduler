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

"""Task MR."""

from typing import Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.engine import Engine, ProgramType


class MR(Engine):
    """Task mr object, declare behavior for mr task to dolphinscheduler."""

    _task_custom_attr = {
        "app_name",
        "main_args",
        "others",
    }

    def __init__(
        self,
        name: str,
        main_class: str,
        main_package: str,
        program_type: Optional[ProgramType] = ProgramType.SCALA,
        app_name: Optional[str] = None,
        main_args: Optional[str] = None,
        others: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(
            name, TaskType.MR, main_class, main_package, program_type, *args, **kwargs
        )
        self.app_name = app_name
        self.main_args = main_args
        self.others = others
