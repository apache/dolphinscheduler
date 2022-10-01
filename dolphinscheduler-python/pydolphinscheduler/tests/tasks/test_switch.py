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

"""Test Task switch."""

from typing import Optional, Tuple
from unittest.mock import patch

import pytest

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.tasks.switch import (
    Branch,
    Default,
    Switch,
    SwitchBranch,
    SwitchCondition,
)
from tests.testing.task import Task

TEST_NAME = "test-task"
TEST_TYPE = "test-type"


def task_switch_arg_wrapper(obj, task: Task, exp: Optional[str] = None) -> SwitchBranch:
    """Wrap task switch and its subclass."""
    if obj is Default:
        return obj(task)
    elif obj is Branch:
        return obj(exp, task)
    else:
        return obj(task, exp)


@pytest.mark.parametrize(
    "obj",
    [
        SwitchBranch,
        Branch,
        Default,
    ],
)
def test_switch_branch_attr_next_node(obj: SwitchBranch):
    """Test get attribute from class switch branch."""
    task = Task(name=TEST_NAME, task_type=TEST_TYPE)
    switch_branch = task_switch_arg_wrapper(obj, task=task, exp="unittest")
    assert switch_branch.next_node == task.code


@pytest.mark.parametrize(
    "obj",
    [
        SwitchBranch,
        Default,
    ],
)
def test_switch_branch_get_define_without_condition(obj: SwitchBranch):
    """Test function :func:`get_define` with None value of attribute condition from class switch branch."""
    task = Task(name=TEST_NAME, task_type=TEST_TYPE)
    expect = {"nextNode": task.code}
    switch_branch = task_switch_arg_wrapper(obj, task=task)
    assert switch_branch.get_define() == expect


@pytest.mark.parametrize(
    "obj",
    [
        SwitchBranch,
        Branch,
    ],
)
def test_switch_branch_get_define_condition(obj: SwitchBranch):
    """Test function :func:`get_define` with specific attribute condition from class switch branch."""
    task = Task(name=TEST_NAME, task_type=TEST_TYPE)
    exp = "${var} == 1"
    expect = {
        "nextNode": task.code,
        "condition": exp,
    }
    switch_branch = task_switch_arg_wrapper(obj, task=task, exp=exp)
    assert switch_branch.get_define() == expect


@pytest.mark.parametrize(
    "args, msg",
    [
        (
            (1,),
            ".*?parameter only support SwitchBranch but got.*?",
        ),
        (
            (Default(Task(TEST_NAME, TEST_TYPE)), 2),
            ".*?parameter only support SwitchBranch but got.*?",
        ),
        (
            (Default(Task(TEST_NAME, TEST_TYPE)), Default(Task(TEST_NAME, TEST_TYPE))),
            ".*?parameter only support exactly one default branch",
        ),
        (
            (
                Branch(condition="unittest", task=Task(TEST_NAME, TEST_TYPE)),
                Default(Task(TEST_NAME, TEST_TYPE)),
                Default(Task(TEST_NAME, TEST_TYPE)),
            ),
            ".*?parameter only support exactly one default branch",
        ),
    ],
)
def test_switch_condition_set_define_attr_error(args: Tuple, msg: str):
    """Test error case on :class:`SwitchCondition`."""
    switch_condition = SwitchCondition(*args)
    with pytest.raises(PyDSParamException, match=msg):
        switch_condition.set_define_attr()


def test_switch_condition_set_define_attr_default():
    """Test set :class:`Default` to attribute on :class:`SwitchCondition`."""
    task = Task(TEST_NAME, TEST_TYPE)
    switch_condition = SwitchCondition(Default(task))
    switch_condition.set_define_attr()
    assert getattr(switch_condition, "next_node") == task.code
    assert getattr(switch_condition, "depend_task_list") == []


def test_switch_condition_set_define_attr_branch():
    """Test set :class:`Branch` to attribute on :class:`SwitchCondition`."""
    task = Task(TEST_NAME, TEST_TYPE)
    switch_condition = SwitchCondition(
        Branch("unittest1", task), Branch("unittest2", task)
    )
    expect = [
        {"condition": "unittest1", "nextNode": task.code},
        {"condition": "unittest2", "nextNode": task.code},
    ]

    switch_condition.set_define_attr()
    assert getattr(switch_condition, "next_node") == ""
    assert getattr(switch_condition, "depend_task_list") == expect


def test_switch_condition_set_define_attr_mix_branch_and_default():
    """Test set bot :class:`Branch` and :class:`Default` to attribute on :class:`SwitchCondition`."""
    task = Task(TEST_NAME, TEST_TYPE)
    switch_condition = SwitchCondition(
        Branch("unittest1", task), Branch("unittest2", task), Default(task)
    )
    expect = [
        {"condition": "unittest1", "nextNode": task.code},
        {"condition": "unittest2", "nextNode": task.code},
    ]

    switch_condition.set_define_attr()
    assert getattr(switch_condition, "next_node") == task.code
    assert getattr(switch_condition, "depend_task_list") == expect


def test_switch_condition_get_define_default():
    """Test function :func:`get_define` with :class:`Default` in :class:`SwitchCondition`."""
    task = Task(TEST_NAME, TEST_TYPE)
    switch_condition = SwitchCondition(Default(task))
    expect = {
        "dependTaskList": [],
        "nextNode": task.code,
    }
    assert switch_condition.get_define() == expect


def test_switch_condition_get_define_branch():
    """Test function :func:`get_define` with :class:`Branch` in :class:`SwitchCondition`."""
    task = Task(TEST_NAME, TEST_TYPE)
    switch_condition = SwitchCondition(
        Branch("unittest1", task), Branch("unittest2", task)
    )
    expect = {
        "dependTaskList": [
            {"condition": "unittest1", "nextNode": task.code},
            {"condition": "unittest2", "nextNode": task.code},
        ],
        "nextNode": "",
    }
    assert switch_condition.get_define() == expect


def test_switch_condition_get_define_mix_branch_and_default():
    """Test function :func:`get_define` with both :class:`Branch` and :class:`Default`."""
    task = Task(TEST_NAME, TEST_TYPE)
    switch_condition = SwitchCondition(
        Branch("unittest1", task), Branch("unittest2", task), Default(task)
    )
    expect = {
        "dependTaskList": [
            {"condition": "unittest1", "nextNode": task.code},
            {"condition": "unittest2", "nextNode": task.code},
        ],
        "nextNode": task.code,
    }
    assert switch_condition.get_define() == expect


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_switch_get_define(mock_task_code_version):
    """Test task switch :func:`get_define`."""
    task = Task(name=TEST_NAME, task_type=TEST_TYPE)
    switch_condition = SwitchCondition(
        Branch(condition="${var1} > 1", task=task),
        Branch(condition="${var1} <= 1", task=task),
        Default(task),
    )

    name = "test_switch_get_define"
    expect = {
        "code": 123,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "SWITCH",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "waitStartTimeout": {},
            "switchResult": {
                "dependTaskList": [
                    {"condition": "${var1} > 1", "nextNode": task.code},
                    {"condition": "${var1} <= 1", "nextNode": task.code},
                ],
                "nextNode": task.code,
            },
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

    task = Switch(name, condition=switch_condition)
    assert task.get_define() == expect


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_switch_set_dep_workflow(mock_task_code_version):
    """Test task switch set dependence in workflow level."""
    with ProcessDefinition(name="test-switch-set-dep-workflow") as pd:
        parent = Task(name="parent", task_type=TEST_TYPE)
        switch_child_1 = Task(name="switch_child_1", task_type=TEST_TYPE)
        switch_child_2 = Task(name="switch_child_2", task_type=TEST_TYPE)
        switch_condition = SwitchCondition(
            Branch(condition="${var} > 1", task=switch_child_1),
            Default(task=switch_child_2),
        )

        switch = Switch(name=TEST_NAME, condition=switch_condition)
        parent >> switch
        # General tasks test
        assert len(pd.tasks) == 4
        assert sorted(pd.task_list, key=lambda t: t.name) == sorted(
            [parent, switch, switch_child_1, switch_child_2], key=lambda t: t.name
        )
        # Task dep test
        assert parent._downstream_task_codes == {switch.code}
        assert switch._upstream_task_codes == {parent.code}

        # Switch task dep after ProcessDefinition function get_define called
        assert switch._downstream_task_codes == {
            switch_child_1.code,
            switch_child_2.code,
        }
        assert all(
            [
                child._upstream_task_codes == {switch.code}
                for child in [switch_child_1, switch_child_2]
            ]
        )
