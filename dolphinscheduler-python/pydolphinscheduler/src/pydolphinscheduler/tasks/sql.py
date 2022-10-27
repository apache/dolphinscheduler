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

import logging
import re
from typing import Dict, Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.database import Database
from pydolphinscheduler.core.task import Task

log = logging.getLogger(__file__)


class SqlType:
    """SQL type, for now it just contain `SELECT` and `NO_SELECT`."""

    SELECT = "0"
    NOT_SELECT = "1"


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

    _task_custom_attr = {
        "sql",
        "sql_type",
        "pre_statements",
        "post_statements",
        "display_rows",
    }

    ext: set = {".sql"}
    ext_attr: str = "_sql"

    def __init__(
        self,
        name: str,
        datasource_name: str,
        sql: str,
        sql_type: Optional[str] = None,
        pre_statements: Optional[str] = None,
        post_statements: Optional[str] = None,
        display_rows: Optional[int] = 10,
        *args,
        **kwargs
    ):
        self._sql = sql
        super().__init__(name, TaskType.SQL, *args, **kwargs)
        self.param_sql_type = sql_type
        self.datasource_name = datasource_name
        self.pre_statements = pre_statements or []
        self.post_statements = post_statements or []
        self.display_rows = display_rows

    @property
    def sql_type(self) -> str:
        """Judgement sql type, it will return the SQL type for type `SELECT` or `NOT_SELECT`.

        If `param_sql_type` dot not specific, will use regexp to check
        which type of the SQL is. But if `param_sql_type` is specific
        will use the parameter overwrites the regexp way
        """
        if (
            self.param_sql_type == SqlType.SELECT
            or self.param_sql_type == SqlType.NOT_SELECT
        ):
            log.info(
                "The sql type is specified by a parameter, with value %s",
                self.param_sql_type,
            )
            return self.param_sql_type
        pattern_select_str = (
            "^(?!(.* |)insert |(.* |)delete |(.* |)drop "
            "|(.* |)update |(.* |)truncate |(.* |)alter |(.* |)create ).*"
        )
        pattern_select = re.compile(pattern_select_str, re.IGNORECASE)
        if pattern_select.match(self._sql) is None:
            return SqlType.NOT_SELECT
        else:
            return SqlType.SELECT

    @property
    def task_params(self, camel_attr: bool = True, custom_attr: set = None) -> Dict:
        """Override Task.task_params for sql task.

        sql task have some specials attribute for task_params, and is odd if we
        directly set as python property, so we Override Task.task_params here.
        """
        params = super().task_params
        datasource = Database(self.datasource_name, "type", "datasource")
        params.update(datasource)
        return params
