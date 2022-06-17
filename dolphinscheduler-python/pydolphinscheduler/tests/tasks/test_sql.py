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

"""Test Task Sql."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.tasks.sql import Sql, SqlType


@pytest.mark.parametrize(
    "sql, param_sql_type, sql_type",
    [
        ("select 1", None, SqlType.SELECT),
        (" select 1", None, SqlType.SELECT),
        (" select 1 ", None, SqlType.SELECT),
        (" select 'insert' ", None, SqlType.SELECT),
        (" select 'insert ' ", None, SqlType.SELECT),
        ("with tmp as (select 1) select * from tmp ", None, SqlType.SELECT),
        (
            "insert into table_name(col1, col2) value (val1, val2)",
            None,
            SqlType.NOT_SELECT,
        ),
        (
            "insert into table_name(select, col2) value ('select', val2)",
            None,
            SqlType.NOT_SELECT,
        ),
        ("update table_name SET col1=val1 where col1=val2", None, SqlType.NOT_SELECT),
        (
            "update table_name SET col1='select' where col1=val2",
            None,
            SqlType.NOT_SELECT,
        ),
        ("delete from table_name where id < 10", None, SqlType.NOT_SELECT),
        ("delete from table_name where id < 10", None, SqlType.NOT_SELECT),
        ("alter table table_name add column col1 int", None, SqlType.NOT_SELECT),
        ("create table table_name2 (col1 int)", None, SqlType.NOT_SELECT),
        ("create table table_name2 (col1 int)", SqlType.SELECT, SqlType.SELECT),
        ("select 1", SqlType.NOT_SELECT, SqlType.NOT_SELECT),
        ("create table table_name2 (col1 int)", SqlType.NOT_SELECT, SqlType.NOT_SELECT),
        ("select 1", SqlType.SELECT, SqlType.SELECT),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.database.Database.get_database_info",
    return_value=({"id": 1, "type": "mock_type"}),
)
def test_get_sql_type(
    mock_datasource, mock_code_version, sql, param_sql_type, sql_type
):
    """Test property sql_type could return correct type."""
    name = "test_get_sql_type"
    datasource_name = "test_datasource"
    task = Sql(name, datasource_name, sql, sql_type=param_sql_type)
    assert (
        sql_type == task.sql_type
    ), f"Sql {sql} expect sql type is {sql_type} but got {task.sql_type}"


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            {"datasource_name": "datasource_name", "sql": "select 1"},
            {
                "sql": "select 1",
                "type": "MYSQL",
                "datasource": 1,
                "sqlType": "0",
                "preStatements": [],
                "postStatements": [],
                "displayRows": 10,
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
    "pydolphinscheduler.core.database.Database.get_database_info",
    return_value=({"id": 1, "type": "MYSQL"}),
)
def test_property_task_params(mock_datasource, mock_code_version, attr, expect):
    """Test task sql task property."""
    task = Sql("test-sql-task-params", **attr)
    assert expect == task.task_params


@patch(
    "pydolphinscheduler.core.database.Database.get_database_info",
    return_value=({"id": 1, "type": "MYSQL"}),
)
def test_sql_get_define(mock_datasource):
    """Test task sql function get_define."""
    code = 123
    version = 1
    name = "test_sql_get_define"
    command = "select 1"
    datasource_name = "test_datasource"
    expect = {
        "code": code,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "SQL",
        "taskParams": {
            "type": "MYSQL",
            "datasource": 1,
            "sql": command,
            "sqlType": "0",
            "displayRows": 10,
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
        task = Sql(name, datasource_name, command)
        assert task.get_define() == expect
