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


java_gateway = launch_gateway()


def get_datasource_info(name):
    """Get datasource info through java gateway."""
    return java_gateway.entry_point.getDatasourceInfo(name)


def get_resources_file_info(program_type, main_package):
    """Get resources file info through java gateway."""
    return java_gateway.entry_point.getResourcesFileInfo(program_type, main_package)


def create_or_update_resource(user_name, name, description, content):
    """Create or update resource through java gateway."""
    return java_gateway.entry_point.createOrUpdateResource(
        user_name, name, description, content
    )


def query_resources_file_info(user_name, name):
    """Get resources file info through java gateway."""
    return java_gateway.entry_point.queryResourcesFileInfo(user_name, name)


def get_code_and_version(project_name, process_definition_name, task_name):
    """Get code and version through java gateway."""
    return java_gateway.entry_point.getCodeAndVersion(
        project_name, process_definition_name, task_name
    )


def create_or_grant_project(user, name, description):
    """Create or grant project through java gateway."""
    return java_gateway.entry_point.createOrGrantProject(user, name, description)


def create_tenant(tenant_name, description, queue_name):
    """Create tenant through java gateway."""
    return java_gateway.entry_point.createTenant(tenant_name, description, queue_name)


def create_user(name, password, email, phone, tenant, queue, status):
    """Create user through java gateway."""
    return java_gateway.entry_point.createUser(
        name, password, email, phone, tenant, queue, status
    )


def get_dependent_info(code_parameter):
    """Get dependent info through java gateway."""
    return java_gateway.entry_point.getDependentInfo(code_parameter)


def get_process_definition_info(user_name, project_name, process_definition_name):
    """Get process definition info through java gateway."""
    return java_gateway.entry_point.getProcessDefinitionInfo(
        user_name, project_name, process_definition_name
    )


def create_or_update_process_definition(
    user_name,
    project_name,
    name,
    description,
    global_params,
    schedule,
    warning_type,
    warning_group_id,
    locations,
    timeout,
    worker_group,
    tenant_code,
    release_state,
    task_relation_json,
    task_definition_json,
    other_params_json,
    execution_type,
):
    """Create or update process definition through java gateway."""
    return java_gateway.entry_point.createOrUpdateProcessDefinition(
        user_name,
        project_name,
        name,
        description,
        global_params,
        schedule,
        warning_type,
        warning_group_id,
        locations,
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
    user_name,
    project_name,
    process_definition_name,
    cron_time,
    worker_group,
    warning_type,
    warning_group_id,
    timeout,
):
    """Exec process instance through java gateway."""
    return java_gateway.entry_point.execProcessInstance(
        user_name,
        project_name,
        process_definition_name,
        cron_time,
        worker_group,
        warning_type,
        warning_group_id,
        timeout,
    )
