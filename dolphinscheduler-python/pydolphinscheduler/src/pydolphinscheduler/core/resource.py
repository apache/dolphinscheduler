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
from pydolphinscheduler.java_gateway import launch_gateway


class Resource:
    """resource object, get information about resource."""

    def __init__(self):
        self._resource = {}

    def get_resource_info(self, program_type, main_package) -> Dict:
        """Get resource info from java gateway, contains resource id, name."""
        if self._resource:
            return self._resource
        else:
            gateway = launch_gateway()
            try:
                self._resource = gateway.entry_point.getResourcesFileInfo(
                    program_type, main_package
                )
            # Handler database source do not exists error, for now we just terminate the process.
            except Py4JJavaError as ex:
                raise PyDSParamException(str(ex.java_exception))
            return self._resource
