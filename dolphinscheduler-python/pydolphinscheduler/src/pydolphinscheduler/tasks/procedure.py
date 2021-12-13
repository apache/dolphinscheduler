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

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.tasks.database import Database


class Procedure(Database):
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

    def __init__(self, name: str, datasource_name: str, sql: str, *args, **kwargs):
        super().__init__(
            TaskType.PROCEDURE, name, datasource_name, sql, *args, **kwargs
        )
