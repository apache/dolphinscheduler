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
from typing import Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.tasks.database import Database


class SqlType:
    """SQL type, for now it just contain `SELECT` and `NO_SELECT`."""

    SELECT = 0
    NOT_SELECT = 1


class Sql(Database):
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

    def __init__(
        self,
        name: str,
        datasource_name: str,
        sql: str,
        pre_statements: Optional[str] = None,
        post_statements: Optional[str] = None,
        display_rows: Optional[int] = 10,
        *args,
        **kwargs
    ):
        super().__init__(TaskType.SQL, name, datasource_name, sql, *args, **kwargs)
        self.pre_statements = pre_statements or []
        self.post_statements = post_statements or []
        self.display_rows = display_rows

    @property
    def sql_type(self) -> int:
        """Judgement sql type, use regexp to check which type of the sql is."""
        pattern_select_str = (
            "^(?!(.* |)insert |(.* |)delete |(.* |)drop |(.* |)update |(.* |)alter ).*"
        )
        pattern_select = re.compile(pattern_select_str, re.IGNORECASE)
        if pattern_select.match(self.sql) is None:
            return SqlType.NOT_SELECT
        else:
            return SqlType.SELECT
