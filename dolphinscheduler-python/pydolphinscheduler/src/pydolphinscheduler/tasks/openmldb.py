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

"""Task OpenMLDB."""

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task


class OpenMLDB(Task):
    """Task OpenMLDB object, declare behavior for OpenMLDB task to dolphinscheduler.

    :param name: task name
    :param zookeeper: OpenMLDB cluster zookeeper address, e.g. 127.0.0.1:2181.
    :param zookeeper_path: OpenMLDB cluster zookeeper path, e.g. /openmldb.
    :param execute_mode: Determine the init mode, offline or online. You can switch it in sql statementself.
    :param sql: SQL statement.
    """

    _task_custom_attr = {
        "zk",
        "zk_path",
        "execute_mode",
        "sql",
    }

    def __init__(
        self, name, zookeeper, zookeeper_path, execute_mode, sql, *args, **kwargs
    ):
        super().__init__(name, TaskType.OPENMLDB, *args, **kwargs)
        self.zk = zookeeper
        self.zk_path = zookeeper_path
        self.execute_mode = execute_mode
        self.sql = sql
