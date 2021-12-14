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

"""Task shell."""

from typing import Optional

from pydolphinscheduler.constants import TaskType
from pydolphinscheduler.core.task import Task
from pydolphinscheduler.exceptions import PyDSParamException


class HttpMethod:
    """Constant of HTTP method."""

    GET = "GET"
    POST = "POST"
    HEAD = "HEAD"
    PUT = "PUT"
    DELETE = "DELETE"


class HttpCheckCondition:
    """Constant of HTTP check condition.

    For now it contain four value:
    - STATUS_CODE_DEFAULT: when http response code equal to 200, mark as success.
    - STATUS_CODE_CUSTOM: when http response code equal to the code user define, mark as success.
    - BODY_CONTAINS: when http response body contain text user define, mark as success.
    - BODY_NOT_CONTAINS: when http response body do not contain text user define, mark as success.
    """

    STATUS_CODE_DEFAULT = "STATUS_CODE_DEFAULT"
    STATUS_CODE_CUSTOM = "STATUS_CODE_CUSTOM"
    BODY_CONTAINS = "BODY_CONTAINS"
    BODY_NOT_CONTAINS = "BODY_NOT_CONTAINS"


class Http(Task):
    """Task HTTP object, declare behavior for HTTP task to dolphinscheduler."""

    _task_custom_attr = {
        "url",
        "http_method",
        "http_params",
        "http_check_condition",
        "condition",
        "connect_timeout",
        "socket_timeout",
    }

    def __init__(
        self,
        name: str,
        url: str,
        http_method: Optional[str] = HttpMethod.GET,
        http_params: Optional[str] = None,
        http_check_condition: Optional[str] = HttpCheckCondition.STATUS_CODE_DEFAULT,
        condition: Optional[str] = None,
        connect_timeout: Optional[int] = 60000,
        socket_timeout: Optional[int] = 60000,
        *args,
        **kwargs
    ):
        super().__init__(name, TaskType.HTTP, *args, **kwargs)
        self.url = url
        if not hasattr(HttpMethod, http_method):
            raise PyDSParamException(
                "Parameter http_method %s not support.", http_method
            )
        self.http_method = http_method
        self.http_params = http_params or []
        if not hasattr(HttpCheckCondition, http_check_condition):
            raise PyDSParamException(
                "Parameter http_check_condition %s not support.", http_check_condition
            )
        self.http_check_condition = http_check_condition
        if (
            http_check_condition != HttpCheckCondition.STATUS_CODE_DEFAULT
            and condition is None
        ):
            raise PyDSParamException(
                "Parameter condition must provider if http_check_condition not equal to STATUS_CODE_DEFAULT"
            )
        self.condition = condition
        self.connect_timeout = connect_timeout
        self.socket_timeout = socket_timeout
