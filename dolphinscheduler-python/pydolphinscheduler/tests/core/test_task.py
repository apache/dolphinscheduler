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

"""Test Task class function."""
import logging
import re
from typing import Set
from unittest.mock import PropertyMock, patch

import pytest

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.core.task import Task, TaskRelation
from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin import Local
from tests.testing.task import Task as TestTask
from tests.testing.task import TaskWithCode

TEST_TASK_RELATION_SET = set()
TEST_TASK_RELATION_SIZE = 0


@pytest.mark.parametrize(
    "addition, ignore, expect",
    [
        (
            set(),
            set(),
            {
                "local_params",
                "resource_list",
                "dependence",
                "wait_start_timeout",
                "condition_result",
            },
        ),
        (
            set(),
            {"dependence", "condition_result", "not_exists"},
            {
                "local_params",
                "resource_list",
                "wait_start_timeout",
            },
        ),
        (
            {
                "not_exists_1",
                "not_exists_2",
            },
            set(),
            {
                "not_exists_1",
                "not_exists_2",
                "local_params",
                "resource_list",
                "dependence",
                "wait_start_timeout",
                "condition_result",
            },
        ),
        # test addition and ignore conflict to see behavior
        (
            {
                "not_exists",
            },
            {"condition_result", "not_exists"},
            {
                "not_exists",
                "local_params",
                "resource_list",
                "dependence",
                "wait_start_timeout",
            },
        ),
    ],
)
def test__get_attr(addition: Set, ignore: Set, expect: Set):
    """Test task function `_get_attr`."""
    task = TestTask(
        name="test-get-attr",
        task_type="test",
    )
    task._task_custom_attr = addition
    task._task_ignore_attr = ignore
    assert task._get_attr() == expect


@pytest.mark.parametrize(
    "attr, expect",
    [
        (
            dict(),
            {
                "localParams": [],
                "resourceList": [],
                "dependence": {},
                "waitStartTimeout": {},
                "conditionResult": {"successNode": [""], "failedNode": [""]},
            },
        ),
        (
            {
                "local_params": ["foo", "bar"],
                "resource_list": ["foo", "bar"],
                "dependence": {"foo", "bar"},
                "wait_start_timeout": {"foo", "bar"},
                "condition_result": {"foo": ["bar"]},
            },
            {
                "localParams": ["foo", "bar"],
                "resourceList": [{"id": 1}],
                "dependence": {"foo", "bar"},
                "waitStartTimeout": {"foo", "bar"},
                "conditionResult": {"foo": ["bar"]},
            },
        ),
    ],
)
@patch(
    "pydolphinscheduler.core.resource.Resource.get_id_from_database",
    return_value=1,
)
@patch(
    "pydolphinscheduler.core.task.Task.user_name",
    return_value="test_user",
)
def test_property_task_params(mock_resource, mock_user_name, attr, expect):
    """Test class task property."""
    task = TestTask(
        "test-property-task-params",
        "test-task",
        **attr,
    )
    assert expect == task.task_params


@pytest.mark.parametrize(
    "pre_code, post_code, expect",
    [
        (123, 456, hash("123 -> 456")),
        (12345678, 987654321, hash("12345678 -> 987654321")),
    ],
)
def test_task_relation_hash_func(pre_code, post_code, expect):
    """Test TaskRelation magic function :func:`__hash__`."""
    task_param = TaskRelation(pre_task_code=pre_code, post_task_code=post_code)
    assert hash(task_param) == expect


@pytest.mark.parametrize(
    "pre_code, post_code, size_add",
    [
        (123, 456, 1),
        (123, 456, 0),
        (456, 456, 1),
        (123, 123, 1),
        (456, 123, 1),
        (0, 456, 1),
        (123, 0, 1),
    ],
)
def test_task_relation_add_to_set(pre_code, post_code, size_add):
    """Test TaskRelation with different pre_code and post_code add to set behavior.

    Here we use global variable to keep set of :class:`TaskRelation` instance and the number we expect
    of the size when we add a new task relation to exists set.
    """
    task_relation = TaskRelation(pre_task_code=pre_code, post_task_code=post_code)
    TEST_TASK_RELATION_SET.add(task_relation)
    # hint python interpreter use global variable instead of local's
    global TEST_TASK_RELATION_SIZE
    TEST_TASK_RELATION_SIZE += size_add
    assert len(TEST_TASK_RELATION_SET) == TEST_TASK_RELATION_SIZE


def test_task_relation_to_dict():
    """Test TaskRelation object function to_dict."""
    pre_task_code = 123
    post_task_code = 456
    expect = {
        "name": "",
        "preTaskCode": pre_task_code,
        "postTaskCode": post_task_code,
        "preTaskVersion": 1,
        "postTaskVersion": 1,
        "conditionType": 0,
        "conditionParams": {},
    }
    task_relation = TaskRelation(
        pre_task_code=pre_task_code, post_task_code=post_task_code
    )
    assert task_relation.get_define() == expect


def test_task_get_define():
    """Test Task object function get_define."""
    code = 123
    version = 1
    name = "test_task_get_define"
    task_type = "test_task_get_define_type"
    expect = {
        "code": code,
        "name": name,
        "version": version,
        "description": None,
        "delayTime": 0,
        "taskType": task_type,
        "taskParams": {
            "resourceList": [],
            "localParams": [],
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
        task = Task(name=name, task_type=task_type)
        assert task.get_define() == expect


@pytest.mark.parametrize("shift", ["<<", ">>"])
def test_two_tasks_shift(shift: str):
    """Test bit operator between tasks.

    Here we test both `>>` and `<<` bit operator.
    """
    upstream = TestTask(name="upstream", task_type=shift)
    downstream = TestTask(name="downstream", task_type=shift)
    if shift == "<<":
        downstream << upstream
    elif shift == ">>":
        upstream >> downstream
    else:
        assert False, f"Unexpect bit operator type {shift}."
    assert (
        1 == len(upstream._downstream_task_codes)
        and downstream.code in upstream._downstream_task_codes
    ), "Task downstream task attributes error, downstream codes size or specific code failed."
    assert (
        1 == len(downstream._upstream_task_codes)
        and upstream.code in downstream._upstream_task_codes
    ), "Task upstream task attributes error, upstream codes size or upstream code failed."


@pytest.mark.parametrize(
    "dep_expr, flag",
    [
        ("task << tasks", "upstream"),
        ("tasks << task", "downstream"),
        ("task >> tasks", "downstream"),
        ("tasks >> task", "upstream"),
    ],
)
def test_tasks_list_shift(dep_expr: str, flag: str):
    """Test bit operator between task and sequence of tasks.

    Here we test both `>>` and `<<` bit operator.
    """
    reverse_dict = {
        "upstream": "downstream",
        "downstream": "upstream",
    }
    task_type = "dep_task_and_tasks"
    task = TestTask(name="upstream", task_type=task_type)
    tasks = [
        TestTask(name="downstream1", task_type=task_type),
        TestTask(name="downstream2", task_type=task_type),
    ]

    # Use build-in function eval to simply test case and reduce duplicate code
    eval(dep_expr)
    direction_attr = f"_{flag}_task_codes"
    reverse_direction_attr = f"_{reverse_dict[flag]}_task_codes"
    assert 2 == len(getattr(task, direction_attr))
    assert [t.code in getattr(task, direction_attr) for t in tasks]

    assert all([1 == len(getattr(t, reverse_direction_attr)) for t in tasks])
    assert all([task.code in getattr(t, reverse_direction_attr) for t in tasks])


def test_add_duplicate(caplog):
    """Test add task which code already in process definition."""
    with ProcessDefinition("test_add_duplicate_workflow") as _:
        TaskWithCode(name="test_task_1", task_type="test", code=123, version=1)
        with caplog.at_level(logging.WARNING):
            TaskWithCode(
                name="test_task_duplicate_code", task_type="test", code=123, version=2
            )
        assert all(
            [
                caplog.text.startswith("WARNING  pydolphinscheduler"),
                re.findall("already in process definition", caplog.text),
            ]
        )


@pytest.mark.parametrize(
    "val, expected",
    [
        ("a.sh", "echo Test task attribute ext_attr"),
        ("a.zsh", "echo Test task attribute ext_attr"),
        ("echo Test task attribute ext_attr", "echo Test task attribute ext_attr"),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.task.Task.ext",
    new_callable=PropertyMock,
    return_value={".sh", ".zsh"},
)
@patch(
    "pydolphinscheduler.core.task.Task.ext_attr",
    new_callable=PropertyMock,
    return_value="_raw_script",
)
@patch(
    "pydolphinscheduler.core.task.Task._raw_script",
    create=True,
    new_callable=PropertyMock,
)
@patch("pydolphinscheduler.core.task.Task.get_plugin")
def test_task_ext_attr(
    m_plugin, m_raw_script, m_ext_attr, m_ext, m_code_version, val, expected
):
    """Test task attribute ext_attr."""
    m_plugin.return_value.read_file.return_value = expected
    m_raw_script.return_value = val
    task = Task("test_task_ext_attr", "test_task_ext_attr")
    assert expected == getattr(task, "raw_script")


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "name": "test_task_abtain_res_plugin",
                "task_type": "TaskType",
                "resource_plugin": Local("prefix"),
                "process_definition": ProcessDefinition(
                    name="process_definition",
                    resource_plugin=Local("prefix"),
                ),
            },
            "Local",
        ),
        (
            {
                "name": "test_task_abtain_res_plugin",
                "task_type": "TaskType",
                "resource_plugin": Local("prefix"),
            },
            "Local",
        ),
        (
            {
                "name": "test_task_abtain_res_plugin",
                "task_type": "TaskType",
                "process_definition": ProcessDefinition(
                    name="process_definition",
                    resource_plugin=Local("prefix"),
                ),
            },
            "Local",
        ),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch("pydolphinscheduler.core.task.Task.get_content")
def test_task_obtain_res_plugin(m_get_content, m_code_version, attr, expected):
    """Test task obtaining resource plug-in."""
    task = Task(**attr)
    assert expected == task.get_plugin().__class__.__name__


@pytest.mark.parametrize(
    "attr",
    [
        {
            "name": "test_task_abtain_res_plugin",
            "task_type": "TaskType",
            "process_definition": ProcessDefinition(
                name="process_definition",
            ),
        },
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch("pydolphinscheduler.core.task.Task.get_content")
def test_task_obtain_res_plugin_exception(m_get_content, m_code_version, attr):
    """Test task obtaining resource plug-in exception."""
    with pytest.raises(
        PyResPluginException,
        match="The execution command of this task is a file, but the resource plugin is empty",
    ):
        task = Task(**attr)
        task.get_plugin()


@pytest.mark.parametrize(
    "resources, expect",
    [
        (
            ["/dev/test.py"],
            [{"id": 1}],
        ),
        (
            ["/dev/test.py", {"id": 2}],
            [{"id": 1}, {"id": 2}],
        ),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.resource.Resource.get_id_from_database",
    return_value=1,
)
@patch(
    "pydolphinscheduler.core.task.Task.user_name",
    return_value="test_user",
)
def test_python_resource_list(
    mock_code_version, mock_resource, mock_user_name, resources, expect
):
    """Test python task resource list."""
    task = Task(
        name="python_resource_list.",
        task_type="PYTHON",
        resource_list=resources,
    )
    assert task.resource_list == expect
