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

"""Test Task dependent."""
from typing import List, Tuple
from unittest.mock import patch

import pytest

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.tasks.condition import (
    FAILURE,
    SUCCESS,
    And,
    Condition,
    ConditionOperator,
    Or,
    Status,
)
from tests.testing.task import Task

TEST_NAME = "test-name"
TEST_PROJECT = "test-project"
TEST_PROCESS_DEFINITION = "test-process-definition"
TEST_TYPE = "test-type"
TEST_PROJECT_CODE, TEST_DEFINITION_CODE, TEST_TASK_CODE = 12345, 123456, 1234567

TEST_OPERATOR_LIST = ("AND", "OR")


@pytest.mark.parametrize(
    "obj, expect",
    [
        (Status, "STATUS"),
        (SUCCESS, "SUCCESS"),
        (FAILURE, "FAILURE"),
    ],
)
def test_class_status_status_name(obj: Status, expect: str):
    """Test class status and sub class property status_name."""
    assert obj.status_name() == expect


@pytest.mark.parametrize(
    "obj, tasks",
    [
        (Status, (1, 2, 3)),
        (SUCCESS, (1.1, 2.2, 3.3)),
        (FAILURE, (ConditionOperator(1), ConditionOperator(2), ConditionOperator(3))),
    ],
)
def test_class_status_depend_item_list_no_expect_type(obj: Status, tasks: Tuple):
    """Test class status and sub class raise error when assign not support type."""
    with pytest.raises(
        PyDSParamException, match=".*?only accept class Task or sub class Task, but get"
    ):
        obj(*tasks).get_define()


@pytest.mark.parametrize(
    "obj, tasks",
    [
        (Status, [Task(str(i), TEST_TYPE) for i in range(1)]),
        (Status, [Task(str(i), TEST_TYPE) for i in range(2)]),
        (Status, [Task(str(i), TEST_TYPE) for i in range(3)]),
        (SUCCESS, [Task(str(i), TEST_TYPE) for i in range(1)]),
        (SUCCESS, [Task(str(i), TEST_TYPE) for i in range(2)]),
        (SUCCESS, [Task(str(i), TEST_TYPE) for i in range(3)]),
        (FAILURE, [Task(str(i), TEST_TYPE) for i in range(1)]),
        (FAILURE, [Task(str(i), TEST_TYPE) for i in range(2)]),
        (FAILURE, [Task(str(i), TEST_TYPE) for i in range(3)]),
    ],
)
def test_class_status_depend_item_list(obj: Status, tasks: Tuple):
    """Test class status and sub class function :func:`depend_item_list`."""
    status = obj.status_name()
    expect = [
        {
            "depTaskCode": i.code,
            "status": status,
        }
        for i in tasks
    ]
    assert obj(*tasks).get_define() == expect


@pytest.mark.parametrize(
    "obj, expect",
    [
        (ConditionOperator, "CONDITIONOPERATOR"),
        (And, "AND"),
        (Or, "OR"),
    ],
)
def test_condition_operator_operator_name(obj: ConditionOperator, expect: str):
    """Test class ConditionOperator and sub class class function :func:`operator_name`."""
    assert obj.operator_name() == expect


@pytest.mark.parametrize(
    "obj, expect",
    [
        (ConditionOperator, "CONDITIONOPERATOR"),
        (And, "AND"),
        (Or, "OR"),
    ],
)
def test_condition_operator_relation(obj: ConditionOperator, expect: str):
    """Test class ConditionOperator and sub class class property `relation`."""
    assert obj(1).relation == expect


@pytest.mark.parametrize(
    "obj, status_or_operator, match",
    [
        (
            ConditionOperator,
            [Status(Task("1", TEST_TYPE)), 1],
            ".*?operator parameter support ConditionTask and ConditionOperator.*?",
        ),
        (
            ConditionOperator,
            [
                Status(Task("1", TEST_TYPE)),
                1.0,
            ],
            ".*?operator parameter support ConditionTask and ConditionOperator.*?",
        ),
        (
            ConditionOperator,
            [
                Status(Task("1", TEST_TYPE)),
                ConditionOperator(And(Status(Task("1", TEST_TYPE)))),
            ],
            ".*?operator parameter only support same type.",
        ),
        (
            ConditionOperator,
            [
                ConditionOperator(And(Status(Task("1", TEST_TYPE)))),
                Status(Task("1", TEST_TYPE)),
            ],
            ".*?operator parameter only support same type.",
        ),
    ],
)
def test_condition_operator_set_define_attr_not_support_type(
    obj, status_or_operator, match
):
    """Test class ConditionOperator parameter error, including parameter not same or type not support."""
    with pytest.raises(PyDSParamException, match=match):
        op = obj(*status_or_operator)
        op.set_define_attr()


@pytest.mark.parametrize(
    "obj, task_num",
    [
        (ConditionOperator, 1),
        (ConditionOperator, 2),
        (ConditionOperator, 3),
        (And, 1),
        (And, 2),
        (And, 3),
        (Or, 1),
        (Or, 2),
        (Or, 3),
    ],
)
def test_condition_operator_set_define_attr_status(
    obj: ConditionOperator, task_num: int
):
    """Test :func:`set_define_attr` with one or more class status."""
    attr = "depend_item_list"

    tasks = [Task(str(i), TEST_TYPE) for i in range(task_num)]
    status = Status(*tasks)

    expect = [
        {"depTaskCode": task.code, "status": status.status_name()} for task in tasks
    ]

    co = obj(status)
    co.set_define_attr()
    assert getattr(co, attr) == expect


@pytest.mark.parametrize(
    "obj, status",
    [
        (ConditionOperator, (SUCCESS, SUCCESS)),
        (ConditionOperator, (FAILURE, FAILURE)),
        (ConditionOperator, (SUCCESS, FAILURE)),
        (ConditionOperator, (FAILURE, SUCCESS)),
        (And, (SUCCESS, SUCCESS)),
        (And, (FAILURE, FAILURE)),
        (And, (SUCCESS, FAILURE)),
        (And, (FAILURE, SUCCESS)),
        (Or, (SUCCESS, SUCCESS)),
        (Or, (FAILURE, FAILURE)),
        (Or, (SUCCESS, FAILURE)),
        (Or, (FAILURE, SUCCESS)),
    ],
)
def test_condition_operator_set_define_attr_mix_status(
    obj: ConditionOperator, status: List[Status]
):
    """Test :func:`set_define_attr` with one or more mixed status."""
    attr = "depend_item_list"

    task = Task("test-operator", TEST_TYPE)
    status_list = []
    expect = []
    for sta in status:
        status_list.append(sta(task))
        expect.append({"depTaskCode": task.code, "status": sta.status_name()})

    co = obj(*status_list)
    co.set_define_attr()
    assert getattr(co, attr) == expect


@pytest.mark.parametrize(
    "obj, task_num",
    [
        (ConditionOperator, 1),
        (ConditionOperator, 2),
        (ConditionOperator, 3),
        (And, 1),
        (And, 2),
        (And, 3),
        (Or, 1),
        (Or, 2),
        (Or, 3),
    ],
)
def test_condition_operator_set_define_attr_operator(
    obj: ConditionOperator, task_num: int
):
    """Test :func:`set_define_attr` with one or more class condition operator."""
    attr = "depend_task_list"

    task = Task("test-operator", TEST_TYPE)
    status = Status(task)

    expect = [
        {
            "relation": obj.operator_name(),
            "dependItemList": [
                {
                    "depTaskCode": task.code,
                    "status": status.status_name(),
                }
            ],
        }
        for _ in range(task_num)
    ]

    co = obj(*[obj(status) for _ in range(task_num)])
    co.set_define_attr()
    assert getattr(co, attr) == expect


@pytest.mark.parametrize(
    "cond, sub_cond",
    [
        (ConditionOperator, (And, Or)),
        (ConditionOperator, (Or, And)),
        (And, (And, Or)),
        (And, (Or, And)),
        (Or, (And, Or)),
        (Or, (Or, And)),
    ],
)
def test_condition_operator_set_define_attr_mix_operator(
    cond: ConditionOperator, sub_cond: Tuple[ConditionOperator]
):
    """Test :func:`set_define_attr` with one or more class mix condition operator."""
    attr = "depend_task_list"

    task = Task("test-operator", TEST_TYPE)

    expect = []
    sub_condition = []
    for cond in sub_cond:
        status = Status(task)
        sub_condition.append(cond(status))
        expect.append(
            {
                "relation": cond.operator_name(),
                "dependItemList": [
                    {
                        "depTaskCode": task.code,
                        "status": status.status_name(),
                    }
                ],
            }
        )
    co = cond(*sub_condition)
    co.set_define_attr()
    assert getattr(co, attr) == expect


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(12345, 1),
)
@patch(
    "pydolphinscheduler.tasks.condition.Condition.gen_code_and_version",
    return_value=(123, 1),
)
def test_condition_get_define(mock_condition_code_version, mock_task_code_version):
    """Test task condition :func:`get_define`."""
    common_task = Task(name="common_task", task_type="test_task_condition")
    cond_operator = And(
        And(
            SUCCESS(common_task, common_task),
            FAILURE(common_task, common_task),
        ),
        Or(
            SUCCESS(common_task, common_task),
            FAILURE(common_task, common_task),
        ),
    )

    name = "test_condition_get_define"
    expect = {
        "code": 123,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "CONDITIONS",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "dependence": {
                "relation": "AND",
                "dependTaskList": [
                    {
                        "relation": "AND",
                        "dependItemList": [
                            {"depTaskCode": common_task.code, "status": "SUCCESS"},
                            {"depTaskCode": common_task.code, "status": "SUCCESS"},
                            {"depTaskCode": common_task.code, "status": "FAILURE"},
                            {"depTaskCode": common_task.code, "status": "FAILURE"},
                        ],
                    },
                    {
                        "relation": "OR",
                        "dependItemList": [
                            {"depTaskCode": common_task.code, "status": "SUCCESS"},
                            {"depTaskCode": common_task.code, "status": "SUCCESS"},
                            {"depTaskCode": common_task.code, "status": "FAILURE"},
                            {"depTaskCode": common_task.code, "status": "FAILURE"},
                        ],
                    },
                ],
            },
            "conditionResult": {
                "successNode": [common_task.code],
                "failedNode": [common_task.code],
            },
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

    task = Condition(
        name, condition=cond_operator, success_task=common_task, failed_task=common_task
    )
    assert task.get_define() == expect


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_condition_set_dep_workflow(mock_task_code_version):
    """Test task condition set dependence in workflow level."""
    with ProcessDefinition(name="test-condition-set-dep-workflow") as pd:
        pre_task_1 = Task(name="pre_task_1", task_type=TEST_TYPE)
        pre_task_2 = Task(name="pre_task_2", task_type=TEST_TYPE)
        pre_task_3 = Task(name="pre_task_3", task_type=TEST_TYPE)
        cond_operator = And(
            And(
                SUCCESS(pre_task_1, pre_task_2),
                FAILURE(pre_task_3),
            ),
        )

        success_branch = Task(name="success_branch", task_type=TEST_TYPE)
        fail_branch = Task(name="fail_branch", task_type=TEST_TYPE)

        condition = Condition(
            name="conditions",
            condition=cond_operator,
            success_task=success_branch,
            failed_task=fail_branch,
        )

        # General tasks test
        assert len(pd.tasks) == 6
        assert sorted(pd.task_list, key=lambda t: t.name) == sorted(
            [
                pre_task_1,
                pre_task_2,
                pre_task_3,
                success_branch,
                fail_branch,
                condition,
            ],
            key=lambda t: t.name,
        )
        # Task dep test
        assert success_branch._upstream_task_codes == {condition.code}
        assert fail_branch._upstream_task_codes == {condition.code}
        assert condition._downstream_task_codes == {
            success_branch.code,
            fail_branch.code,
        }

        # Condition task dep after ProcessDefinition function get_define called
        assert condition._upstream_task_codes == {
            pre_task_1.code,
            pre_task_2.code,
            pre_task_3.code,
        }
        assert all(
            [
                child._downstream_task_codes == {condition.code}
                for child in [
                    pre_task_1,
                    pre_task_2,
                    pre_task_3,
                ]
            ]
        )
