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

"""Task procedure."""

from typing import Dict

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.database import Database
from pydolphinscheduler.core.task import Task


class Procedure(Task):
    """Task Procedure object, declare behavior for Procedure task to dolphinscheduler.

    It should run database procedure job in multiply sql lik engine, such as:
    - ClickHouse
    - DB2
    - HIVE
    - MySQL
    - Oracle
    - Postgresql
    - Presto
    - SQLServer
    You provider datasource_name contain connection information, it decisions which
    database type and database instance would run this sql.
    """

    _task_custom_attr = {"method"}

    def __init__(self, name: str, datasource_name: str, method: str, *args, **kwargs):
        super().__init__(name, TaskType.PROCEDURE, *args, **kwargs)
        self.datasource_name = datasource_name
        self.method = method

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for produce task.

        produce task have some specials attribute for task_params, and is odd if we
        directly set as python property, so we Override Task.task_params here.
        """
        params = super().task_params
        datasource = Database(self.datasource_name, "type", "datasource")
        params.update(datasource)
        return params
