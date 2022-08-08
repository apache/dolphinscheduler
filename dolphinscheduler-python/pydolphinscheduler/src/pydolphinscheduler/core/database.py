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

"""Module database."""

from typing import Dict

from py4j.protocol import Py4JJavaError

from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.java_gateway import JavaGate


class Database(dict):
    """database object, get information about database.

    You provider database_name contain connection information, it decisions which
    database type and database instance would run task.
    """

    def __init__(self, database_name: str, type_key, database_key, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._database = {}
        self.database_name = database_name
        self[type_key] = self.database_type
        self[database_key] = self.database_id

    @property
    def database_type(self) -> str:
        """Get database type from java gateway, a wrapper for :func:`get_database_info`."""
        return self.get_database_info(self.database_name).get("type")

    @property
    def database_id(self) -> str:
        """Get database id from java gateway, a wrapper for :func:`get_database_info`."""
        return self.get_database_info(self.database_name).get("id")

    def get_database_info(self, name) -> Dict:
        """Get database info from java gateway, contains database id, type, name."""
        if self._database:
            return self._database
        else:
            try:
                self._database = JavaGate().get_datasource_info(name)
            # Handler database source do not exists error, for now we just terminate the process.
            except Py4JJavaError as ex:
                raise PyDSParamException(str(ex.java_exception))
            return self._database
