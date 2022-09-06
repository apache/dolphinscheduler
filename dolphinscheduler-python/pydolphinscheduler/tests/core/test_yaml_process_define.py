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

"""Test YAML process."""

import os
from pathlib import Path
from unittest.mock import patch

import pytest

from pydolphinscheduler import configuration, tasks
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.core.yaml_process_define import (
    ParseTool,
    create_process_definition,
    get_task_cls,
)
from pydolphinscheduler.exceptions import PyDSTaskNoFoundException
from tests.testing.path import path_yaml_example
from tests.testing.task import Task


@pytest.mark.parametrize(
    "string_param, expect",
    [
        ("$ENV{PROJECT_NAME}", "~/pydolphinscheduler"),
    ],
)
def test_parse_tool_env_exist(string_param, expect):
    """Test parsing the environment variable."""
    os.environ["PROJECT_NAME"] = expect
    assert expect == ParseTool.parse_string_param_if_env(string_param)


def test_parse_tool_env_not_exist():
    """Test parsing the not exist environment variable."""
    key = "THIS_ENV_NOT_EXIST_0000000"
    string_param = "$ENV{%s}" % key
    expect = "$" + key
    assert expect == ParseTool.parse_string_param_if_env(string_param)


@pytest.mark.parametrize(
    "string_param, expect_key",
    [
        ("${CONFIG.java_gateway.address}", "java_gateway.address"),
        ("${CONFIG.WORKFLOW_PROJECT}", "default.workflow.project"),
    ],
)
def test_parse_tool_config(string_param, expect_key):
    """Test parsing configuration."""
    expect = configuration.get_single_config(expect_key)
    assert expect == ParseTool.parse_string_param_if_config(string_param)


def test_parse_possible_yaml_file():
    """Test parsing possible path."""
    folder = Path(path_yaml_example)
    file_name = "Shell.yaml"
    path = folder.joinpath(file_name)

    with open(path, "r") as f:
        expect = "".join(f)

    string_param = '$FILE{"%s"}' % file_name
    content_ = ParseTool.parse_string_param_if_file(string_param, base_folder=folder)

    assert expect == content_


def test_parse_tool_parse_possible_path_file():
    """Test parsing possible path."""
    folder = Path(path_yaml_example)
    file_name = "Shell.yaml"
    path = folder.joinpath(file_name)

    possible_path = ParseTool.get_possible_path(path, base_folder=folder)
    assert path == possible_path

    possible_path = ParseTool.get_possible_path(file_name, base_folder=folder)
    assert path == possible_path

    possible_path = ParseTool.get_possible_path(file_name, base_folder=".")
    assert path != possible_path


@pytest.mark.parametrize(
    "task_type, expect",
    [
        ("shell", tasks.Shell),
        ("Shell", tasks.Shell),
        ("ShEll", tasks.Shell),
        ("Condition", tasks.Condition),
        ("DataX", tasks.DataX),
        ("CustomDataX", tasks.CustomDataX),
        ("Dependent", tasks.Dependent),
        ("Flink", tasks.Flink),
        ("Http", tasks.Http),
        ("MR", tasks.MR),
        ("Procedure", tasks.Procedure),
        ("Python", tasks.Python),
        ("Shell", tasks.Shell),
        ("Spark", tasks.Spark),
        ("Sql", tasks.Sql),
        ("SubProcess", tasks.SubProcess),
        ("Switch", tasks.Switch),
        ("SageMaker", tasks.SageMaker),
    ],
)
def test_get_task(task_type, expect):
    """Test get task function."""
    assert expect == get_task_cls(task_type)


@pytest.mark.parametrize(
    "task_type",
    [
        ("MYSQL"),
    ],
)
def test_get_error(task_type):
    """Test get task cls error."""
    with pytest.raises(
        PyDSTaskNoFoundException,
        match=f"not find task {task_type}",
    ):
        get_task_cls(task_type)


@pytest.mark.parametrize(
    "yaml_file",
    [
        ("Condition.yaml"),
        ("DataX.yaml"),
        ("Dependent.yaml"),
        ("Flink.yaml"),
        ("Procedure.yaml"),
        ("Http.yaml"),
        ("MapReduce.yaml"),
        ("Python.yaml"),
        ("Shell.yaml"),
        ("Spark.yaml"),
        ("Sql.yaml"),
        ("SubProcess.yaml"),
        # ("Switch.yaml"),
        ("MoreConfiguration.yaml"),
    ],
)
@patch(
    "pydolphinscheduler.core.engine.Engine.get_resource_info",
    return_value=({"id": 1, "name": "test"}),
)
@patch(
    "pydolphinscheduler.core.database.Database.get_database_info",
    return_value=({"id": 1, "type": "mock_type"}),
)
@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": 0,
        "processDefinitionCode": 0,
        "taskDefinitionCode": 0,
    },
)
@patch.object(ProcessDefinition, "run")
@patch.object(ProcessDefinition, "submit")
def test_get_create_process_definition(
    prun, psubmit, dep_item, db_info, resource_info, yaml_file
):
    """Test create_process_definition function to parse example YAML file."""
    yaml_file_path = Path(path_yaml_example).joinpath(yaml_file)
    with patch(
        "pydolphinscheduler.core.task.Task.gen_code_and_version",
        side_effect=Task("test_func_wrap", "func_wrap").gen_code_and_version,
    ):
        create_process_definition(yaml_file_path)
