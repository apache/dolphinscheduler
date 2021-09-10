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


class ProcessDefinitionReleaseState:
    """
    ProcessDefinition release state
    """

    ONLINE: str = "ONLINE"
    OFFLINE: str = "OFFLINE"


class ProcessDefinitionDefault:
    """
    ProcessDefinition default values
    """

    PROJECT: str = "project-pydolphin"
    TENANT: str = "tenant_pydolphin"
    USER: str = "userPythonGateway"
    # TODO simple set password same as username
    USER_PWD: str = "userPythonGateway"
    USER_EMAIL: str = "userPythonGateway@dolphinscheduler.com"
    USER_PHONE: str = "11111111111"
    USER_STATE: int = 1
    QUEUE: str = "queuePythonGateway"
    WORKER_GROUP: str = "default"


class TaskPriority(str):
    HIGHEST = "HIGHEST"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"
    LOWEST = "LOWEST"


class TaskFlag(str):
    YES = "YES"
    NO = "NO"


class TaskTimeoutFlag(str):
    CLOSE = "CLOSE"


class TaskType(str):
    SHELL = "SHELL"


class DefaultTaskCodeNum(str):
    DEFAULT = 1


class JavaGatewayDefault(str):
    RESULT_MESSAGE_KEYWORD = "msg"
    RESULT_MESSAGE_SUCCESS = "success"

    RESULT_STATUS_KEYWORD = "status"
    RESULT_STATUS_SUCCESS = "SUCCESS"

    RESULT_DATA = "data"
