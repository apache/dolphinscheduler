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

"""Module java gateway, contain gateway behavior."""

from typing import Any, Optional

from py4j.java_collections import JavaMap
from py4j.java_gateway import GatewayParameters, JavaGateway

from pydolphinscheduler import configuration
from pydolphinscheduler.constants import JavaGatewayDefault
from pydolphinscheduler.exceptions import PyDSJavaGatewayException


def launch_gateway(
    address: Optional[str] = None,
    port: Optional[int] = None,
    auto_convert: Optional[bool] = True,
) -> JavaGateway:
    """Launch java gateway to pydolphinscheduler.

    TODO Note that automatic conversion makes calling Java methods slightly less efficient because
    in the worst case, Py4J needs to go through all registered converters for all parameters.
    This is why automatic conversion is disabled by default.
    """
    gateway_parameters = GatewayParameters(
        address=address or configuration.JAVA_GATEWAY_ADDRESS,
        port=port or configuration.JAVA_GATEWAY_PORT,
        auto_convert=auto_convert or configuration.JAVA_GATEWAY_AUTO_CONVERT,
    )
    gateway = JavaGateway(gateway_parameters=gateway_parameters)
    return gateway


def gateway_result_checker(
    result: JavaMap,
    msg_check: Optional[str] = JavaGatewayDefault.RESULT_MESSAGE_SUCCESS,
) -> Any:
    """Check weather java gateway result success or not."""
    if (
        result[JavaGatewayDefault.RESULT_STATUS_KEYWORD].toString()
        != JavaGatewayDefault.RESULT_STATUS_SUCCESS
    ):
        raise PyDSJavaGatewayException("Failed when try to got result for java gateway")
    if (
        msg_check is not None
        and result[JavaGatewayDefault.RESULT_MESSAGE_KEYWORD] != msg_check
    ):
        raise PyDSJavaGatewayException("Get result state not success.")
    return result


class JavaGate:
    """Launch java gateway to pydolphin scheduler."""

    def __init__(
        self,
        address: Optional[str] = None,
        port: Optional[int] = None,
        auto_convert: Optional[bool] = True,
    ):
        self.java_gateway = launch_gateway(address, port, auto_convert)

    def get_datasource_info(self, name: str):
        """Get datasource info through java gateway."""
        return self.java_gateway.entry_point.getDatasourceInfo(name)

    def get_resources_file_info(self, program_type: str, main_package: str):
        """Get resources file info through java gateway."""
        return self.java_gateway.entry_point.getResourcesFileInfo(
            program_type, main_package
        )

    def create_or_update_resource(
        self, user_name: str, name: str, content: str, description: Optional[str] = None
    ):
        """Create or update resource through java gateway."""
        return self.java_gateway.entry_point.createOrUpdateResource(
            user_name, name, description, content
        )

    def query_resources_file_info(self, user_name: str, name: str):
        """Get resources file info through java gateway."""
        return self.java_gateway.entry_point.queryResourcesFileInfo(user_name, name)

    def query_environment_info(self, name: str):
        """Get environment info through java gateway."""
        return self.java_gateway.entry_point.getEnvironmentInfo(name)

    def get_code_and_version(
        self, project_name: str, process_definition_name: str, task_name: str
    ):
        """Get code and version through java gateway."""
        return self.java_gateway.entry_point.getCodeAndVersion(
            project_name, process_definition_name, task_name
        )

    def create_or_grant_project(
        self, user: str, name: str, description: Optional[str] = None
    ):
        """Create or grant project through java gateway."""
        return self.java_gateway.entry_point.createOrGrantProject(
            user, name, description
        )

    def create_tenant(
        self, tenant_name: str, queue_name: str, description: Optional[str] = None
    ):
        """Create tenant through java gateway."""
        return self.java_gateway.entry_point.createTenant(
            tenant_name, description, queue_name
        )

    def create_user(
        self,
        name: str,
        password: str,
        email: str,
        phone: str,
        tenant: str,
        queue: str,
        status: int,
    ):
        """Create user through java gateway."""
        return self.java_gateway.entry_point.createUser(
            name, password, email, phone, tenant, queue, status
        )

    def get_dependent_info(
        self,
        project_name: str,
        process_definition_name: str,
        task_name: Optional[str] = None,
    ):
        """Get dependent info through java gateway."""
        return self.java_gateway.entry_point.getDependentInfo(
            project_name, process_definition_name, task_name
        )

    def get_process_definition_info(
        self, user_name: str, project_name: str, process_definition_name: str
    ):
        """Get process definition info through java gateway."""
        return self.java_gateway.entry_point.getProcessDefinitionInfo(
            user_name, project_name, process_definition_name
        )

    def create_or_update_process_definition(
        self,
        user_name: str,
        project_name: str,
        name: str,
        description: str,
        global_params: str,
        warning_type: str,
        warning_group_id: int,
        timeout: int,
        worker_group: str,
        tenant_code: str,
        release_state: int,
        task_relation_json: str,
        task_definition_json: str,
        schedule: Optional[str] = None,
        other_params_json: Optional[str] = None,
        execution_type: Optional[str] = None,
    ):
        """Create or update process definition through java gateway."""
        return self.java_gateway.entry_point.createOrUpdateProcessDefinition(
            user_name,
            project_name,
            name,
            description,
            global_params,
            schedule,
            warning_type,
            warning_group_id,
            timeout,
            worker_group,
            tenant_code,
            release_state,
            task_relation_json,
            task_definition_json,
            other_params_json,
            execution_type,
        )

    def exec_process_instance(
        self,
        user_name: str,
        project_name: str,
        process_definition_name: str,
        cron_time: str,
        worker_group: str,
        warning_type: str,
        warning_group_id: int,
        timeout: int,
    ):
        """Exec process instance through java gateway."""
        return self.java_gateway.entry_point.execProcessInstance(
            user_name,
            project_name,
            process_definition_name,
            cron_time,
            worker_group,
            warning_type,
            warning_group_id,
            timeout,
        )
