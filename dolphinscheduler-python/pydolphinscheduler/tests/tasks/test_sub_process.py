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

"""Test Task sub_process."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.sub_process import SubProcess

TEST_SUB_PROCESS_DEFINITION_NAME = "sub-test-process-definition"
TEST_SUB_PROCESS_DEFINITION_CODE = "3643589832320"
TEST_PROCESS_DEFINITION_NAME = "simple-test-process-definition"


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {"process_definition_name": TEST_SUB_PROCESS_DEFINITION_NAME},
            {
                "processDefinitionCode": TEST_SUB_PROCESS_DEFINITION_CODE,
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "waitStartTimeout": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
            },
        )
    ],
)
@patch(
    "pydolphinscheduler.tasks.sub_process.SubProcess.get_process_definition_info",
    return_value=(
        {
            "id": 1,
            "name": TEST_SUB_PROCESS_DEFINITION_NAME,
            "code": TEST_SUB_PROCESS_DEFINITION_CODE,
        }
    ),
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_property_task_params(mock_code_version, mock_pd_info, attr, expect):
    """Test task sub process property."""
    task = SubProcess("test-sub-process-task-params", **attr)
    assert expect == task.task_params


@patch(
    "pydolphinscheduler.tasks.sub_process.SubProcess.get_process_definition_info",
    return_value=(
        {
            "id": 1,
            "name": TEST_SUB_PROCESS_DEFINITION_NAME,
            "code": TEST_SUB_PROCESS_DEFINITION_CODE,
        }
    ),
)
def test_sub_process_get_define(mock_process_definition):
    """Test task sub_process function get_define."""
    code = 123
    version = 1
    name = "test_sub_process_get_define"
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "SUB_PROCESS",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "processDefinitionCode": TEST_SUB_PROCESS_DEFINITION_CODE,
            "dependence": {},
            "conditionResult": {"successNode": [""], "failedNode": [""]},
            "waitStartTimeout": {},
        },
        "flag": "YES",
        "taskPriority": "MEDIUM",
        "workerGroup": "default",
        "environmentCode": None,
        "failRetryTimes": 0,
        "failRetryInterval": 1,
        "timeoutFlag": "CLOSE",
        "timeoutNotifyStrategy": None,
        "timeout": 0,
    }
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(code, version),
    ):
        with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME):
            sub_process = SubProcess(name, TEST_SUB_PROCESS_DEFINITION_NAME)
            assert sub_process.get_define() == expect
