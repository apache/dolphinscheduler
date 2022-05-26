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

from datetime import datetime
from typing import Any
from unittest.mock import patch

import pytest
from freezegun import freeze_time

from pydolphinscheduler.core import configuration
from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.side import Project, Tenant, User
from pydolphinscheduler.tasks.switch import Branch, Default, Switch, SwitchCondition
from pydolphinscheduler.utils.date import conv_to_schedule
from tests.testing.task import Task

TEST_PROCESS_DEFINITION_NAME = "simple-test-process-definition"
TEST_TASK_TYPE = "test-task-type"


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
        ("timezone", configuration.WORKFLOW_TIME_ZONE),
        ("project", Project(configuration.WORKFLOW_PROJECT)),
        ("tenant", Tenant(configuration.WORKFLOW_TENANT)),
        (
            "user",
            User(
                configuration.USER_NAME,
                configuration.USER_PASSWORD,
                configuration.USER_EMAIL,
                configuration.USER_PHONE,
                configuration.WORKFLOW_TENANT,
                configuration.WORKFLOW_QUEUE,
                configuration.USER_STATE,
            ),
        ),
        ("worker_group", configuration.WORKFLOW_WORKER_GROUP),
        ("warning_type", configuration.WORKFLOW_WARNING_TYPE),
        ("warning_group_id", 0),
        ("release_state", 1),
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
        ("name", str, "name"),
        ("description", str, "description"),
        ("schedule", str, "schedule"),
        ("timezone", str, "timezone"),
        ("worker_group", str, "worker_group"),
        ("warning_type", str, "FAILURE"),
        ("warning_group_id", int, 1),
        ("timeout", int, 1),
        ("param", dict, {"key": "value"}),
    ],
)
def test_set_attr(name, cls, expect):
    """Test process definition set attributes which get with same type."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        setattr(pd, name, expect)
        assert (
            getattr(pd, name) == expect
        ), f"ProcessDefinition set attribute `{name}` do not work expect"


@pytest.mark.parametrize(
    "value,expect",
    [
        ("online", 1),
        ("offline", 0),
    ],
)
def test_set_release_state(value, expect):
    """Test process definition set release_state attributes."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME, release_state=value) as pd:
        assert (
            getattr(pd, "release_state") == expect
        ), "ProcessDefinition set attribute release_state do not return expect value."


@pytest.mark.parametrize(
    "value",
    [
        "oneline",
        "offeline",
        1,
        0,
        None,
    ],
)
def test_set_release_state_error(value):
    """Test process definition set release_state attributes with error."""
    pd = ProcessDefinition(TEST_PROCESS_DEFINITION_NAME, release_state=value)
    with pytest.raises(
        PyDSParamException,
        match="Parameter release_state only support `online` or `offline` but get.*",
    ):
        pd.release_state


@pytest.mark.parametrize(
    "set_attr,set_val,get_attr,get_val",
    [
        ("_project", "project", "project", Project("project")),
        ("_tenant", "tenant", "tenant", Tenant("tenant")),
        ("_start_time", "2021-01-01", "start_time", datetime(2021, 1, 1)),
        ("_end_time", "2021-01-01", "end_time", datetime(2021, 1, 1)),
    ],
)
def test_set_attr_return_special_object(set_attr, set_val, get_attr, get_val):
    """Test process definition set attributes which get with different type."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        setattr(pd, set_attr, set_val)
        assert get_val == getattr(
            pd, get_attr
        ), f"Set attribute {set_attr} can not get back with {get_val}."


@pytest.mark.parametrize(
    "val,expect",
    [
        (datetime(2021, 1, 1), datetime(2021, 1, 1)),
        (None, None),
        ("2021-01-01", datetime(2021, 1, 1)),
        ("2021-01-01 01:01:01", datetime(2021, 1, 1, 1, 1, 1)),
    ],
)
def test__parse_datetime(val, expect):
    """Test process definition function _parse_datetime.

    Only two datetime test cases here because we have more test cases in tests/utils/test_date.py file.
    """
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        assert expect == pd._parse_datetime(
            val
        ), f"Function _parse_datetime with unexpect value by {val}."


@pytest.mark.parametrize(
    "val",
    [
        20210101,
        (2021, 1, 1),
        {"year": "2021", "month": "1", "day": 1},
    ],
)
def test__parse_datetime_not_support_type(val: Any):
    """Test process definition function _parse_datetime not support type error."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        with pytest.raises(PyDSParamException, match="Do not support value type.*?"):
            pd._parse_datetime(val)


@pytest.mark.parametrize(
    "val",
    [
        "ALLL",
        "nonee",
    ],
)
def test_warn_type_not_support_type(val: str):
    """Test process definition param warning_type not support type error."""
    with pytest.raises(
        PyDSParamException, match="Parameter `warning_type` with unexpect value.*?"
    ):
        ProcessDefinition(TEST_PROCESS_DEFINITION_NAME, warning_type=val)


@pytest.mark.parametrize(
    "param, expect",
    [
        (
            None,
            [],
        ),
        (
            {},
            [],
        ),
        (
            {"key1": "val1"},
            [
                {
                    "prop": "key1",
                    "direct": "IN",
                    "type": "VARCHAR",
                    "value": "val1",
                }
            ],
        ),
        (
            {
                "key1": "val1",
                "key2": "val2",
            },
            [
                {
                    "prop": "key1",
                    "direct": "IN",
                    "type": "VARCHAR",
                    "value": "val1",
                },
                {
                    "prop": "key2",
                    "direct": "IN",
                    "type": "VARCHAR",
                    "value": "val2",
                },
            ],
        ),
    ],
)
def test_property_param_json(param, expect):
    """Test ProcessDefinition's property param_json."""
    pd = ProcessDefinition(TEST_PROCESS_DEFINITION_NAME, param=param)
    assert pd.param_json == expect


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test__pre_submit_check_switch_without_param(mock_code_version):
    """Test :func:`_pre_submit_check` if process definition with switch but without attribute param."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        parent = Task(name="parent", task_type=TEST_TASK_TYPE)
        switch_child_1 = Task(name="switch_child_1", task_type=TEST_TASK_TYPE)
        switch_child_2 = Task(name="switch_child_2", task_type=TEST_TASK_TYPE)
        switch_condition = SwitchCondition(
            Branch(condition="${var} > 1", task=switch_child_1),
            Default(task=switch_child_2),
        )

        switch = Switch(name="switch", condition=switch_condition)
        parent >> switch
        with pytest.raises(
            PyDSParamException,
            match="Parameter param or at least one local_param of task must "
            "be provider if task Switch in process definition.",
        ):
            pd._pre_submit_check()


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test__pre_submit_check_switch_with_local_params(mock_code_version):
    """Test :func:`_pre_submit_check` if process definition with switch with local params of task."""
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        parent = Task(
            name="parent",
            task_type=TEST_TASK_TYPE,
            local_params=[
                {"prop": "var", "direct": "OUT", "type": "VARCHAR", "value": ""}
            ],
        )
        switch_child_1 = Task(name="switch_child_1", task_type=TEST_TASK_TYPE)
        switch_child_2 = Task(name="switch_child_2", task_type=TEST_TASK_TYPE)
        switch_condition = SwitchCondition(
            Branch(condition="${var} > 1", task=switch_child_1),
            Default(task=switch_child_2),
        )

        switch = Switch(name="switch", condition=switch_condition)
        parent >> switch
        pd._pre_submit_check()


def test_process_definition_get_define_without_task():
    """Test process definition function get_define without task."""
    expect = {
        "name": TEST_PROCESS_DEFINITION_NAME,
        "description": None,
        "project": configuration.WORKFLOW_PROJECT,
        "tenant": configuration.WORKFLOW_TENANT,
        "workerGroup": configuration.WORKFLOW_WORKER_GROUP,
        "warningType": configuration.WORKFLOW_WARNING_TYPE,
        "warningGroupId": 0,
        "timeout": 0,
        "releaseState": 1,
        "param": None,
        "tasks": {},
        "taskDefinitionJson": [{}],
        "taskRelationJson": [{}],
    }
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        assert pd.get_define() == expect


def test_process_definition_simple_context_manager():
    """Test simple create workflow in process definition context manager mode."""
    expect_tasks_num = 5
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME) as pd:
        for i in range(expect_tasks_num):
            curr_task = Task(name=f"task-{i}", task_type=f"type-{i}")
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


def test_process_definition_simple_separate():
    """Test process definition simple create workflow in separate mode.

    This test just test basic information, cause most of test case is duplicate to
    test_process_definition_simple_context_manager.
    """
    expect_tasks_num = 5
    pd = ProcessDefinition(TEST_PROCESS_DEFINITION_NAME)
    for i in range(expect_tasks_num):
        curr_task = Task(
            name=f"task-{i}",
            task_type=f"type-{i}",
            process_definition=pd,
        )
        # Set deps task i as i-1 parent
        if i > 0:
            pre_task = pd.get_one_task_by_name(f"task-{i - 1}")
            curr_task.set_upstream(pre_task)
    assert len(pd.tasks) == expect_tasks_num
    assert all(["task-" in task.name for task in pd.task_list])


@pytest.mark.parametrize(
    "user_attrs",
    [
        {"tenant": "tenant_specific"},
    ],
)
def test_set_process_definition_user_attr(user_attrs):
    """Test user with correct attributes if we specific assigned to process definition object."""
    default_value = {
        "tenant": configuration.WORKFLOW_TENANT,
    }
    with ProcessDefinition(TEST_PROCESS_DEFINITION_NAME, **user_attrs) as pd:
        user = pd.user
        for attr in default_value:
            # Get assigned attribute if we specific, else get default value
            except_attr = (
                user_attrs[attr] if attr in user_attrs else default_value[attr]
            )
            # Get actually attribute of user object
            actual_attr = getattr(user, attr)
            assert (
                except_attr == actual_attr
            ), f"Except attribute is {except_attr} but get {actual_attr}"


def test_schedule_json_none_schedule():
    """Test function schedule_json with None as schedule."""
    with ProcessDefinition(
        TEST_PROCESS_DEFINITION_NAME,
        schedule=None,
    ) as pd:
        assert pd.schedule_json is None


# We freeze time here, because we test start_time with None, and if will get datetime.datetime.now. If we do
# not freeze time, it will cause flaky test here.
@freeze_time("2021-01-01")
@pytest.mark.parametrize(
    "start_time,end_time,expect_date",
    [
        (
            "20210101",
            "20210201",
            {"start_time": "2021-01-01 00:00:00", "end_time": "2021-02-01 00:00:00"},
        ),
        (
            "2021-01-01",
            "2021-02-01",
            {"start_time": "2021-01-01 00:00:00", "end_time": "2021-02-01 00:00:00"},
        ),
        (
            "2021/01/01",
            "2021/02/01",
            {"start_time": "2021-01-01 00:00:00", "end_time": "2021-02-01 00:00:00"},
        ),
        # Test mix pattern
        (
            "2021/01/01 01:01:01",
            "2021-02-02 02:02:02",
            {"start_time": "2021-01-01 01:01:01", "end_time": "2021-02-02 02:02:02"},
        ),
        (
            "2021/01/01 01:01:01",
            "20210202 020202",
            {"start_time": "2021-01-01 01:01:01", "end_time": "2021-02-02 02:02:02"},
        ),
        (
            "20210101 010101",
            "2021-02-02 02:02:02",
            {"start_time": "2021-01-01 01:01:01", "end_time": "2021-02-02 02:02:02"},
        ),
        # Test None value
        (
            "2021/01/01 01:02:03",
            None,
            {"start_time": "2021-01-01 01:02:03", "end_time": "9999-12-31 23:59:59"},
        ),
        (
            None,
            None,
            {
                "start_time": conv_to_schedule(datetime(2021, 1, 1)),
                "end_time": "9999-12-31 23:59:59",
            },
        ),
    ],
)
def test_schedule_json_start_and_end_time(start_time, end_time, expect_date):
    """Test function schedule_json about handle start_time and end_time.

    Only two datetime test cases here because we have more test cases in tests/utils/test_date.py file.
    """
    schedule = "0 0 0 * * ? *"
    expect = {
        "crontab": schedule,
        "startTime": expect_date["start_time"],
        "endTime": expect_date["end_time"],
        "timezoneId": configuration.WORKFLOW_TIME_ZONE,
    }
    with ProcessDefinition(
        TEST_PROCESS_DEFINITION_NAME,
        schedule=schedule,
        start_time=start_time,
        end_time=end_time,
        timezone=configuration.WORKFLOW_TIME_ZONE,
    ) as pd:
        assert pd.schedule_json == expect
