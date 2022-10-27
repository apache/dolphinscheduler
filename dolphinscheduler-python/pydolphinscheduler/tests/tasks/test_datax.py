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

"""Test Task DataX."""
from pathlib import Path
from unittest.mock import patch

import pytest

from pydolphinscheduler.resources_plugin import Local
from pydolphinscheduler.tasks.datax import CustomDataX, DataX
from pydolphinscheduler.utils import file
from tests.testing.file import delete_file


@pytest.fixture()
def setup_crt_first(request):
    """Set up and teardown about create file first and then delete it."""
    file_content = request.param.get("file_content")
    file_path = request.param.get("file_path")
    file.write(
        content=file_content,
        to_path=file_path,
    )
    yield
    delete_file(file_path)


@patch(
    "pydolphinscheduler.core.database.Database.get_database_info",
    return_value=({"id": 1, "type": "MYSQL"}),
)
def test_datax_get_define(mock_datasource):
    """Test task datax function get_define."""
    code = 123
    version = 1
    name = "test_datax_get_define"
    command = "select name from test_source_table_name"
    datasource_name = "test_datasource"
    datatarget_name = "test_datatarget"
    target_table = "test_target_table_name"
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "DATAX",
        "taskParams": {
            "customConfig": 0,
            "dsType": "MYSQL",
            "dataSource": 1,
            "dtType": "MYSQL",
            "dataTarget": 1,
            "sql": command,
            "targetTable": target_table,
            "jobSpeedByte": 0,
            "jobSpeedRecord": 1000,
            "xms": 1,
            "xmx": 1,
            "preStatements": [],
            "postStatements": [],
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
    }
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(code, version),
    ):
        task = DataX(name, datasource_name, datatarget_name, command, target_table)
        assert task.get_define() == expect


@pytest.mark.parametrize("json_template", ["json_template"])
def test_custom_datax_get_define(json_template):
    """Test task custom datax function get_define."""
    code = 123
    version = 1
    name = "test_custom_datax_get_define"
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "DATAX",
        "taskParams": {
            "customConfig": 1,
            "json": json_template,
            "xms": 1,
            "xmx": 1,
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
    }
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        return_value=(code, version),
    ):
        task = CustomDataX(name, json_template)
        assert task.get_define() == expect


@pytest.mark.parametrize(
    "setup_crt_first",
    [
        {
            "file_path": Path(__file__).parent.joinpath("local_res.sql"),
            "file_content": "test local resource",
        }
    ],
    indirect=True,
)
@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "name": "task_datax",
                "datasource_name": "first_mysql",
                "datatarget_name": "second_mysql",
                "sql": "local_res.sql",
                "target_table": "target_table",
                "resource_plugin": Local(str(Path(__file__).parent)),
            },
            "test local resource",
        ),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_resources_local_datax_command_content(
    mock_code_version, attr, expect, setup_crt_first
):
    """Test task datax sql content through the local resource plug-in."""
    datax = DataX(**attr)
    assert expect == getattr(datax, "sql")


@pytest.mark.parametrize(
    "setup_crt_first",
    [
        {
            "file_path": Path(__file__).parent.joinpath("local_res.json"),
            "file_content": '{content: "test local resource"}',
        }
    ],
    indirect=True,
)
@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {
                "name": "task_custom_datax",
                "json": "local_res.json",
                "resource_plugin": Local(str(Path(__file__).parent)),
            },
            '{content: "test local resource"}',
        ),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_resources_local_custom_datax_command_content(
    mock_code_version, attr, expect, setup_crt_first
):
    """Test task CustomDataX json content through the local resource plug-in."""
    custom_datax = CustomDataX(**attr)
    assert expect == getattr(custom_datax, "json")
