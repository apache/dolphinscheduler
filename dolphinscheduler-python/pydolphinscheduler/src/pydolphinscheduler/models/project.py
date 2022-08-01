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

"""DolphinScheduler Project object."""

from typing import Optional

from pydolphinscheduler import configuration
from pydolphinscheduler.java_gateway import launch_gateway
from pydolphinscheduler.models import BaseSide


class Project(BaseSide):
    """DolphinScheduler Project object."""

    def __init__(
        self,
        name: str = configuration.WORKFLOW_PROJECT,
        description: Optional[str] = None,
        code: Optional[str] = None,
    ):
        super().__init__(name, description)
        self.code = code

    def create_if_not_exists(self, user=configuration.USER_NAME) -> None:
        """Create Project if not exists."""
        gateway = launch_gateway()
        project = gateway.entry_point.createOrGrantProject(user, self.name, self.description)
        self.code = project.projectCode
        # TODO recover result checker
        # gateway_result_checker(result, None)

    def get_project_by_name(self, user=configuration.USER_NAME, name=None) -> None:
        """Get Project by name."""
        gateway = launch_gateway()
        project = gateway.entry_point.getProjectByName(user, name)
        self.name = project.name
        self.description = project.description
        self.code = project.code
        return

    def update(self, user=configuration.USER_NAME, project_code=None, project_name=None, description=None) -> None:
        """Update Project."""
        gateway = launch_gateway()
        gateway.entry_point.updateProject(user, project_code, project_name, description)
        self.name = project_name
        self.description = description

    def delete(self, user=configuration.USER_NAME) -> None:
        """Delete Project."""
        gateway = launch_gateway()
        gateway.entry_point.deleteProject(user, self.code)
