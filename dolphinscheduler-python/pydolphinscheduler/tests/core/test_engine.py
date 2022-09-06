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

"""Test Task Engine."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.core.engine import Engine, ProgramType

TEST_ENGINE_TASK_TYPE = "ENGINE"
TEST_MAIN_CLASS = "org.apache.examples.mock.Mock"
TEST_MAIN_PACKAGE = "Mock.jar"
TEST_PROGRAM_TYPE = ProgramType.JAVA


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.engine.Engine.get_resource_info",
    return_value=({"id": 1, "name": "mock_name"}),
)
def test_get_jar_detail(mock_resource, mock_code_version):
    """Test :func:`get_jar_id` can return expect value."""
    name = "test_get_jar_detail"
    task = Engine(
        name,
        TEST_ENGINE_TASK_TYPE,
        TEST_MAIN_CLASS,
        TEST_MAIN_PACKAGE,
        TEST_PROGRAM_TYPE,
    )
    assert 1 == task.get_jar_id()


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "name": "test-task-params",
                "task_type": "test-engine",
                "main_class": "org.apache.examples.mock.Mock",
                "main_package": "TestMock.jar",
                "program_type": ProgramType.JAVA,
            },
            {
                "mainClass": "org.apache.examples.mock.Mock",
                "mainJar": {
                    "id": 1,
                },
                "programType": ProgramType.JAVA,
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
                "waitStartTimeout": {},
            },
        )
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.engine.Engine.get_resource_info",
    return_value=({"id": 1, "name": "mock_name"}),
)
def test_property_task_params(mock_resource, mock_code_version, attr, expect):
    """Test task engine task property."""
    task = Engine(**attr)
    assert expect == task.task_params


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "name": "test-task-test_engine_get_define",
                "task_type": "test-engine",
                "main_class": "org.apache.examples.mock.Mock",
                "main_package": "TestMock.jar",
                "program_type": ProgramType.JAVA,
            },
            {
                "code": 123,
                "name": "test-task-test_engine_get_define",
                "version": 1,
                "description": None,
                "delayTime": 0,
                "taskType": "test-engine",
                "taskParams": {
                    "mainClass": "org.apache.examples.mock.Mock",
                    "mainJar": {
                        "id": 1,
                    },
                    "programType": ProgramType.JAVA,
                    "localParams": [],
                    "resourceList": [],
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
            },
        )
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.engine.Engine.get_resource_info",
    return_value=({"id": 1, "name": "mock_name"}),
)
def test_engine_get_define(mock_resource, mock_code_version, attr, expect):
    """Test task engine function get_define."""
    task = Engine(**attr)
    assert task.get_define() == expect
