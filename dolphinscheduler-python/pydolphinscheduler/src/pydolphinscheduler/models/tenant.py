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

"""DolphinScheduler Tenant object."""

from typing import Optional

from pydolphinscheduler import configuration
from pydolphinscheduler.java_gateway import JavaGate
from pydolphinscheduler.models import BaseSide


class Tenant(BaseSide):
    """DolphinScheduler Tenant object."""

    def __init__(
        self,
        name: str = configuration.WORKFLOW_TENANT,
        queue: str = configuration.WORKFLOW_QUEUE,
        description: Optional[str] = None,
    ):
        super().__init__(name, description)
        self.queue = queue

    def create_if_not_exists(
        self, queue_name: str, user=configuration.USER_NAME
    ) -> None:
        """Create Tenant if not exists."""
        JavaGate().create_tenant(self.name, queue_name, self.description)
        # gateway_result_checker(result, None)
