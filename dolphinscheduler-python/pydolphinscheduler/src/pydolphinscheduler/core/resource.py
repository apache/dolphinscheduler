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

"""Module resource."""

from typing import Optional

from pydolphinscheduler.core.base import Base
from pydolphinscheduler.java_gateway import launch_gateway


def query_resource_id(user_name, full_name):
    """Get resource id from java gateway."""
    return query_resource(user_name, full_name).getId()


def query_resource(user_name, full_name):
    """Get resource info from java gateway, contains resource id, name."""
    gateway = launch_gateway()
    return gateway.entry_point.queryResourcesFileInfo(user_name, full_name)


class Resource(Base):
    """resource object, will define the resources that you want to create or update.

    :param name: The fullname of resource.Includes path and suffix.
    :param content: The description of resource.
    :param description: The description of resource.
    """

    _DEFINE_ATTR = {"name", "content", "description"}

    def __init__(
        self,
        name: str,
        content: str,
        description: Optional[str] = None,
    ):
        super().__init__(name, description)
        self.content = content
        self._resource_code = None
