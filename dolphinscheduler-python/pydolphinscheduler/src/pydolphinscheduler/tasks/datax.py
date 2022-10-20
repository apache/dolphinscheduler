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

"""Task datax."""

from typing import Dict, List, Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.database import Database
from pydolphinscheduler.core.task import Task


class CustomDataX(Task):
    """Task CustomDatax object, declare behavior for custom DataX task to dolphinscheduler.

    You provider json template for DataX, it can synchronize data according to the template you provided.
    """

    CUSTOM_CONFIG = 1

    _task_custom_attr = {"custom_config", "json", "xms", "xmx"}

    ext: set = {".json"}
    ext_attr: str = "_json"

    def __init__(
        self,
        name: str,
        json: str,
        xms: Optional[int] = 1,
        xmx: Optional[int] = 1,
        *args,
        **kwargs
    ):
        self._json = json
        super().__init__(name, TaskType.DATAX, *args, **kwargs)
        self.custom_config = self.CUSTOM_CONFIG
        self.xms = xms
        self.xmx = xmx


class DataX(Task):
    """Task DataX object, declare behavior for DataX task to dolphinscheduler.

    It should run database datax job in multiply sql link engine, such as:
    - MySQL
    - Oracle
    - Postgresql
    - SQLServer
    You provider datasource_name and datatarget_name contain connection information, it decisions which
    database type and database instance would synchronous data.
    """

    CUSTOM_CONFIG = 0

    _task_custom_attr = {
        "custom_config",
        "sql",
        "target_table",
        "job_speed_byte",
        "job_speed_record",
        "pre_statements",
        "post_statements",
        "xms",
        "xmx",
    }

    ext: set = {".sql"}
    ext_attr: str = "_sql"

    def __init__(
        self,
        name: str,
        datasource_name: str,
        datatarget_name: str,
        sql: str,
        target_table: str,
        job_speed_byte: Optional[int] = 0,
        job_speed_record: Optional[int] = 1000,
        pre_statements: Optional[List[str]] = None,
        post_statements: Optional[List[str]] = None,
        xms: Optional[int] = 1,
        xmx: Optional[int] = 1,
        *args,
        **kwargs
    ):
        self._sql = sql
        super().__init__(name, TaskType.DATAX, *args, **kwargs)
        self.custom_config = self.CUSTOM_CONFIG
        self.datasource_name = datasource_name
        self.datatarget_name = datatarget_name
        self.target_table = target_table
        self.job_speed_byte = job_speed_byte
        self.job_speed_record = job_speed_record
        self.pre_statements = pre_statements or []
        self.post_statements = post_statements or []
        self.xms = xms
        self.xmx = xmx

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for datax task.

        datax task have some specials attribute for task_params, and is odd if we
        directly set as python property, so we Override Task.task_params here.
        """
        params = super().task_params
        datasource = Database(self.datasource_name, "dsType", "dataSource")
        params.update(datasource)

        datatarget = Database(self.datatarget_name, "dtType", "dataTarget")
        params.update(datatarget)
        return params
