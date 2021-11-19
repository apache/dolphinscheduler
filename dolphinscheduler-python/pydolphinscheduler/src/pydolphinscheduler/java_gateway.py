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

from pydolphinscheduler.constants import JavaGatewayDefault


def launch_gateway() -> JavaGateway:
    """Launch java gateway to pydolphinscheduler.

    TODO Note that automatic conversion makes calling Java methods slightly less efficient because
    in the worst case, Py4J needs to go through all registered converters for all parameters.
    This is why automatic conversion is disabled by default.
    """
    gateway = JavaGateway(gateway_parameters=GatewayParameters(auto_convert=True))
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
        raise RuntimeError("Failed when try to got result for java gateway")
    if (
        msg_check is not None
        and result[JavaGatewayDefault.RESULT_MESSAGE_KEYWORD] != msg_check
    ):
        raise ValueError("Get result state not success.")
    return result
