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

"""Task sql."""

import re
from typing import Dict, Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task, TaskParams
from pydolphinscheduler.java_gateway import launch_gateway


class SqlType:
    """SQL type, for now it just contain `SELECT` and `NO_SELECT`."""

    SELECT = 0
    NOT_SELECT = 1


class SqlTaskParams(TaskParams):
    """Parameter only for Sql task type."""

    def __init__(
        self,
        type: str,
        datasource: str,
        sql: str,
        sql_type: Optional[int] = SqlType.NOT_SELECT,
        display_rows: Optional[int] = 10,
        pre_statements: Optional[str] = None,
        post_statements: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(*args, **kwargs)
        self.type = type
        self.datasource = datasource
        self.sql = sql
        self.sql_type = sql_type
        self.display_rows = display_rows
        self.pre_statements = pre_statements or []
        self.post_statements = post_statements or []


class Sql(Task):
    """Task SQL object, declare behavior for SQL task to dolphinscheduler.

    It should run sql job in multiply sql lik engine, such as:
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

    def __init__(
        self,
        name: str,
        datasource_name: str,
        sql: str,
        pre_sql: Optional[str] = None,
        post_sql: Optional[str] = None,
        display_rows: Optional[int] = 10,
        *args,
        **kwargs
    ):
        self._sql = sql
        self._datasource_name = datasource_name
        self._datasource = {}
        task_params = SqlTaskParams(
            type=self.get_datasource_type(),
            datasource=self.get_datasource_id(),
            sql=sql,
            sql_type=self.sql_type,
            display_rows=display_rows,
            pre_statements=pre_sql,
            post_statements=post_sql,
        )
        super().__init__(name, TaskType.SQL, task_params, *args, **kwargs)

    def get_datasource_type(self) -> str:
        """Get datasource type from java gateway, a wrapper for :func:`get_datasource_info`."""
        return self.get_datasource_info(self._datasource_name).get("type")

    def get_datasource_id(self) -> str:
        """Get datasource id from java gateway, a wrapper for :func:`get_datasource_info`."""
        return self.get_datasource_info(self._datasource_name).get("id")

    def get_datasource_info(self, name) -> Dict:
        """Get datasource info from java gateway, contains datasource id, type, name."""
        if self._datasource:
            return self._datasource
        else:
            gateway = launch_gateway()
            self._datasource = gateway.entry_point.getDatasourceInfo(name)
            return self._datasource

    @property
    def sql_type(self) -> int:
        """Judgement sql type, use regexp to check which type of the sql is."""
        pattern_select_str = (
            "^(?!(.* |)insert |(.* |)delete |(.* |)drop |(.* |)update |(.* |)alter ).*"
        )
        pattern_select = re.compile(pattern_select_str, re.IGNORECASE)
        if pattern_select.match(self._sql) is None:
            return SqlType.NOT_SELECT
        else:
            return SqlType.SELECT
