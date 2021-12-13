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

"""Test Task Database."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.tasks.database import Database

TEST_DATABASE_TASK_TYPE = "SQL"
TEST_DATABASE_SQL = "select 1"
TEST_DATABASE_DATASOURCE_NAME = "test_datasource"


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.tasks.database.Database.get_datasource_info",
    return_value=({"id": 1, "type": "mock_type"}),
)
def test_get_datasource_detail(mock_datasource, mock_code_version):
    """Test :func:`get_datasource_type` and :func:`get_datasource_id` can return expect value."""
    name = "test_get_database_detail"
    task = Database(
        TEST_DATABASE_TASK_TYPE, name, TEST_DATABASE_DATASOURCE_NAME, TEST_DATABASE_SQL
    )
    assert 1 == task.get_datasource_id()
    assert "mock_type" == task.get_datasource_type()


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "task_type": TEST_DATABASE_TASK_TYPE,
                "name": "test-task-params",
                "datasource_name": TEST_DATABASE_DATASOURCE_NAME,
                "sql": TEST_DATABASE_SQL,
            },
            {
                "type": "MYSQL",
                "datasource": 1,
                "sql": TEST_DATABASE_SQL,
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
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.tasks.database.Database.get_datasource_info",
    return_value=({"id": 1, "type": "MYSQL"}),
)
def test_property_task_params(mock_datasource, mock_code_version, attr, expect):
    """Test task database task property."""
    task = Database(**attr)
    assert expect == task.task_params


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.tasks.database.Database.get_datasource_info",
    return_value=({"id": 1, "type": "MYSQL"}),
)
def test_database_get_define(mock_datasource, mock_code_version):
    """Test task database function get_define."""
    name = "test_database_get_define"
    expect = {
        "code": 123,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": TEST_DATABASE_TASK_TYPE,
        "taskParams": {
            "type": "MYSQL",
            "datasource": 1,
            "sql": TEST_DATABASE_SQL,
            "localParams": [],
            "resourceList": [],
            "dependence": {},
            "conditionResult": {"successNode": [""], "failedNode": [""]},
            "waitStartTimeout": {},
        },
        "flag": "YES",
        "taskPriority": "MEDIUM",
        "workerGroup": "default",
        "failRetryTimes": 0,
        "failRetryInterval": 1,
        "timeoutFlag": "CLOSE",
        "timeoutNotifyStrategy": None,
        "timeout": 0,
    }
    task = Database(
        TEST_DATABASE_TASK_TYPE, name, TEST_DATABASE_DATASOURCE_NAME, TEST_DATABASE_SQL
    )
    assert task.get_define() == expect
