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

"""DolphinScheduler User object."""

from typing import Optional

from pydolphinscheduler import configuration
from pydolphinscheduler.java_gateway import JavaGate
from pydolphinscheduler.models import BaseSide, Tenant


class User(BaseSide):
    """DolphinScheduler User object."""

    _KEY_ATTR = {
        "name",
        "password",
        "email",
        "phone",
        "tenant",
        "queue",
        "status",
    }

    def __init__(
        self,
        name: str,
        password: Optional[str] = configuration.USER_PASSWORD,
        email: Optional[str] = configuration.USER_EMAIL,
        phone: Optional[str] = configuration.USER_PHONE,
        tenant: Optional[str] = configuration.WORKFLOW_TENANT,
        queue: Optional[str] = configuration.WORKFLOW_QUEUE,
        status: Optional[int] = configuration.USER_STATE,
    ):
        super().__init__(name)
        self.password = password
        self.email = email
        self.phone = phone
        self.tenant = tenant
        self.queue = queue
        self.status = status

    def create_tenant_if_not_exists(self) -> None:
        """Create tenant object."""
        tenant = Tenant(name=self.tenant, queue=self.queue)
        tenant.create_if_not_exists(self.queue)

    def create_if_not_exists(self, **kwargs):
        """Create User if not exists."""
        # Should make sure queue already exists.
        self.create_tenant_if_not_exists()
        JavaGate().create_user(
            self.name,
            self.password,
            self.email,
            self.phone,
            self.tenant,
            self.queue,
            self.status,
        )
        # TODO recover result checker
        # gateway_result_checker(result, None)
