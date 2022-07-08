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


class ResourceDefinition(Base):
    """resource object, will define the resources that you want to create or update.

    :param user: The user who create or update resource.
    :param name: The name of resource.Do not include file suffixes.
    :param suffix: The suffix of resource.
    :param current_dir: The folder where the resource resides.
    :param content: The description of resource.
    :param description: The description of resource.
    """

    _DEFINE_ATTR = {"user", "name", "suffix", "current_dir", "content", "description"}

    def __init__(
        self,
        user: str,
        name: str,
        suffix: str,
        current_dir: str,
        content: str,
        description: Optional[str] = None,
    ):
        super().__init__(name, description)
        self.user = user
        self.suffix = suffix
        self.current_dir = current_dir
        self.content = content
        self._resource_code = None

    def submit(self) -> int:
        """Submit resource to java gateway."""
        gateway = launch_gateway()
        self._resource_code = gateway.entry_point.createOrUpdateResource(
            self.user,
            self.current_dir,
            self.name,
            self.suffix,
            self.description,
            self.content,
        )
        return self._resource_code
