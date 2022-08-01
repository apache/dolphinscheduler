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
from pydolphinscheduler.java_gateway import launch_gateway
from pydolphinscheduler.models import BaseSide


class Tenant(BaseSide):
    """DolphinScheduler Tenant object."""

    def __init__(
        self,
        name: str = configuration.WORKFLOW_TENANT,
        queue: str = configuration.WORKFLOW_QUEUE,
        description: Optional[str] = None,
        tenant_id: Optional[int] = None,
        code: Optional[str] = None,
    ):
        super().__init__(name, description)
        self.tenant_id = tenant_id
        self.queue = queue
        self.code = code

    def create_if_not_exists(
        self, queue_name: str, user=configuration.USER_NAME
    ) -> None:
        """Create Tenant if not exists."""
        gateway = launch_gateway()
        tenant = gateway.entry_point.createTenant(self.name, self.description, queue_name)
        self.tenant_id = tenant.tenantId
        # gateway_result_checker(result, None)

    @staticmethod
    def get_tenant_list(user=configuration.USER_NAME, searchVal=None, pageNo=1, pageSize=10):
        """Get Tenant list."""
        gateway = launch_gateway()
        tenant_list = gateway.entry_point.getTenantList(user, searchVal, pageNo, pageSize)
        return tenant_list

    def update(self, user=configuration.USER_NAME, code=None, queue_id=None, description=None) -> None:
        """Update Tenant."""
        gateway = launch_gateway()
        gateway.entry_point.updateTenant(user, self.tenant_id, code, queue_id, description)
        # TODO: check queue_id and queue_name
        self.queue = str(queue_id)
        self.code = code
        self.description = description
        return

    def delete(self, user=configuration.USER_NAME) -> None:
        """Delete Tenant."""
        gateway = launch_gateway()
        gateway.entry_point.deleteTenant(user, self.tenant_id)
        return
