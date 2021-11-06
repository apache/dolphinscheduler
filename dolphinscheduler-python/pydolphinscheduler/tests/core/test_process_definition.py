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

"""Test process definition."""

import pytest

from pydolphinscheduler.constants import (
    ProcessDefinitionDefault,
    ProcessDefinitionReleaseState,
)
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.core.task import TaskParams
from pydolphinscheduler.side import Tenant, Project, User
from tests.testing.task import Task

TEST_PROCESS_DEFINITION_NAME = "simple-test-process-definition"


@pytest.mark.parametrize("func", ["run", "submit", "start"])
def test_process_definition_key_attr(func):
    """Test process definition have specific functions or attributes."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        assert hasattr(
            pd, func
        ), f"ProcessDefinition instance don't have attribute `{func}`"


@pytest.mark.parametrize(
    "name,value",
    [
        ("project", Project(ProcessDefinitionDefault.PROJECT)),
        ("tenant", Tenant(ProcessDefinitionDefault.TENANT)),
        (
            "user",
            User(
                ProcessDefinitionDefault.USER,
                ProcessDefinitionDefault.USER_PWD,
                ProcessDefinitionDefault.USER_EMAIL,
                ProcessDefinitionDefault.USER_PHONE,
                ProcessDefinitionDefault.TENANT,
                ProcessDefinitionDefault.QUEUE,
                ProcessDefinitionDefault.USER_STATE,
            ),
        ),
        ("worker_group", ProcessDefinitionDefault.WORKER_GROUP),
        ("release_state", ProcessDefinitionReleaseState.ONLINE),
    ],
)
def test_process_definition_default_value(name, value):
    """Test process definition default attributes."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        assert getattr(pd, name) == value, (
            f"ProcessDefinition instance attribute `{name}` not with "
            f"except default value `{getattr(pd, name)}`"
        )


@pytest.mark.parametrize(
    "name,cls,expect",
    [
        ("project", Project, "project"),
        ("tenant", Tenant, "tenant"),
        ("worker_group", str, "worker_group"),
    ],
)
def test_process_definition_set_attr(name, cls, expect):
    """Test process definition set specific attributes."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        setattr(pd, name, cls(expect))
        assert getattr(pd, name) == cls(
            expect
        ), f"ProcessDefinition set attribute `{name}` do not work expect"


def test_process_definition_to_dict_without_task():
    """Test process definition function to_dict without task."""
    expect = {
        "name": TEST_PROCESS_DEFINITION_NAME,
        "description": None,
        "project": ProcessDefinitionDefault.PROJECT,
        "tenant": ProcessDefinitionDefault.TENANT,
        "workerGroup": ProcessDefinitionDefault.WORKER_GROUP,
        "timeout": 0,
        "releaseState": ProcessDefinitionReleaseState.ONLINE,
        "param": None,
        "tasks": {},
        "taskDefinitionJson": [{}],
        "taskRelationJson": [{}],
    }
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        assert pd.to_dict() == expect


def test_process_definition_simple():
    """Test process definition simple create workflow, including process definition, task, relation define."""
    expect_tasks_num = 5
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        for i in range(expect_tasks_num):
            task_params = TaskParams(raw_script=f"test-raw-script-{i}")
            curr_task = Task(
                name=f"task-{i}", task_type=f"type-{i}", task_params=task_params
            )
            # Set deps task i as i-1 parent
            if i > 0:
                pre_task = pd.get_one_task_by_name(f"task-{i - 1}")
                curr_task.set_upstream(pre_task)
        assert len(pd.tasks) == expect_tasks_num

        # Test if task process_definition same as origin one
        task: Task = pd.get_one_task_by_name("task-0")
        assert pd is task.process_definition

        # Test if all tasks with expect deps
        for i in range(expect_tasks_num):
            task: Task = pd.get_one_task_by_name(f"task-{i}")
            if i == 0:
                assert task._upstream_task_codes == set()
                assert task._downstream_task_codes == {
                    pd.get_one_task_by_name("task-1").code
                }
            elif i == expect_tasks_num - 1:
                assert task._upstream_task_codes == {
                    pd.get_one_task_by_name(f"task-{i - 1}").code
                }
                assert task._downstream_task_codes == set()
            else:
                assert task._upstream_task_codes == {
                    pd.get_one_task_by_name(f"task-{i - 1}").code
                }
                assert task._downstream_task_codes == {
                    pd.get_one_task_by_name(f"task-{i + 1}").code
                }
