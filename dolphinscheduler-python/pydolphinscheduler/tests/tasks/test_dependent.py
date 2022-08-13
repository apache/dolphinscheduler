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
import itertools
from typing import Dict, List, Optional, Tuple, Union
from unittest.mock import patch

import pytest

from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.tasks.dependent import (
    And,
    Dependent,
    DependentDate,
    DependentItem,
    DependentOperator,
    Or,
)

TEST_PROJECT = "test-project"
TEST_PROCESS_DEFINITION = "test-process-definition"
TEST_TASK = "test-task"
TEST_PROJECT_CODE, TEST_DEFINITION_CODE, TEST_TASK_CODE = 12345, 123456, 1234567

TEST_OPERATOR_LIST = ("AND", "OR")


@pytest.mark.parametrize(
    "dep_date, dep_cycle",
    [
        # hour
        (DependentDate.CURRENT_HOUR, "hour"),
        (DependentDate.LAST_ONE_HOUR, "hour"),
        (DependentDate.LAST_TWO_HOURS, "hour"),
        (DependentDate.LAST_THREE_HOURS, "hour"),
        (DependentDate.LAST_TWENTY_FOUR_HOURS, "hour"),
        # day
        (DependentDate.TODAY, "day"),
        (DependentDate.LAST_ONE_DAYS, "day"),
        (DependentDate.LAST_TWO_DAYS, "day"),
        (DependentDate.LAST_THREE_DAYS, "day"),
        (DependentDate.LAST_SEVEN_DAYS, "day"),
        # week
        (DependentDate.THIS_WEEK, "week"),
        (DependentDate.LAST_WEEK, "week"),
        (DependentDate.LAST_MONDAY, "week"),
        (DependentDate.LAST_TUESDAY, "week"),
        (DependentDate.LAST_WEDNESDAY, "week"),
        (DependentDate.LAST_THURSDAY, "week"),
        (DependentDate.LAST_FRIDAY, "week"),
        (DependentDate.LAST_SATURDAY, "week"),
        (DependentDate.LAST_SUNDAY, "week"),
        # month
        (DependentDate.THIS_MONTH, "month"),
        (DependentDate.LAST_MONTH, "month"),
        (DependentDate.LAST_MONTH_BEGIN, "month"),
        (DependentDate.LAST_MONTH_END, "month"),
    ],
)
@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": TEST_PROJECT_CODE,
        "processDefinitionCode": TEST_DEFINITION_CODE,
        "taskDefinitionCode": TEST_TASK_CODE,
    },
)
def test_dependent_item_get_define(mock_task_info, dep_date, dep_cycle):
    """Test dependent.DependentItem get define.

    Here we have test some cases as below.
    ```py
    {
        "projectCode": "project code",
        "definitionCode": "definition code",
        "depTaskCode": "dep task code",
        "cycle": "day",
        "dateValue": "today"
    }
    ```
    """
    attr = {
        "project_name": TEST_PROJECT,
        "process_definition_name": TEST_PROCESS_DEFINITION,
        "dependent_task_name": TEST_TASK,
        "dependent_date": dep_date,
    }
    expect = {
        "projectCode": TEST_PROJECT_CODE,
        "definitionCode": TEST_DEFINITION_CODE,
        "depTaskCode": TEST_TASK_CODE,
        "cycle": dep_cycle,
        "dateValue": dep_date,
    }
    task = DependentItem(**attr)
    assert expect == task.get_define()


def test_dependent_item_date_error():
    """Test error when pass None to dependent_date."""
    with pytest.raises(
        PyDSParamException, match="Parameter dependent_date must provider.*?"
    ):
        DependentItem(
            project_name=TEST_PROJECT,
            process_definition_name=TEST_PROCESS_DEFINITION,
            dependent_date=None,
        )


@pytest.mark.parametrize(
    "task_name, result",
    [
        ({"dependent_task_name": TEST_TASK}, TEST_TASK),
        ({}, None),
    ],
)
def test_dependent_item_code_parameter(task_name: dict, result: Optional[str]):
    """Test dependent item property code_parameter."""
    dependent_item = DependentItem(
        project_name=TEST_PROJECT,
        process_definition_name=TEST_PROCESS_DEFINITION,
        **task_name,
    )
    expect = (TEST_PROJECT, TEST_PROCESS_DEFINITION, result)
    assert dependent_item.code_parameter == expect


@pytest.mark.parametrize(
    "arg_list",
    [
        [1, 2],
        [
            DependentItem(
                project_name=TEST_PROJECT,
                process_definition_name=TEST_PROCESS_DEFINITION,
            ),
            1,
        ],
        [
            And(
                DependentItem(
                    project_name=TEST_PROJECT,
                    process_definition_name=TEST_PROCESS_DEFINITION,
                )
            ),
            1,
        ],
        [
            DependentItem(
                project_name=TEST_PROJECT,
                process_definition_name=TEST_PROCESS_DEFINITION,
            ),
            And(
                DependentItem(
                    project_name=TEST_PROJECT,
                    process_definition_name=TEST_PROCESS_DEFINITION,
                )
            ),
        ],
    ],
)
@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": TEST_PROJECT_CODE,
        "processDefinitionCode": TEST_DEFINITION_CODE,
        "taskDefinitionCode": TEST_TASK_CODE,
    },
)
def test_dependent_operator_set_define_error(mock_code, arg_list):
    """Test dependent operator function :func:`set_define` with not support type."""
    dep_op = DependentOperator(*arg_list)
    with pytest.raises(PyDSParamException, match="Dependent .*? operator.*?"):
        dep_op.set_define_attr()


@pytest.mark.parametrize(
    # Test dependent operator, Test dependent item parameters, expect operator define
    "operators, kwargs, expect",
    [
        # Test dependent operator (And | Or) with single dependent item
        (
            (And, Or),
            (
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_MONTH_END,
                },
            ),
            [
                {
                    "relation": op,
                    "dependItemList": [
                        {
                            "projectCode": TEST_PROJECT_CODE,
                            "definitionCode": TEST_DEFINITION_CODE,
                            "depTaskCode": TEST_TASK_CODE,
                            "cycle": "month",
                            "dateValue": DependentDate.LAST_MONTH_END,
                        },
                    ],
                }
                for op in TEST_OPERATOR_LIST
            ],
        ),
        # Test dependent operator (And | Or) with two dependent item
        (
            (And, Or),
            (
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_MONTH_END,
                },
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_WEEK,
                },
            ),
            [
                {
                    "relation": op,
                    "dependItemList": [
                        {
                            "projectCode": TEST_PROJECT_CODE,
                            "definitionCode": TEST_DEFINITION_CODE,
                            "depTaskCode": TEST_TASK_CODE,
                            "cycle": "month",
                            "dateValue": DependentDate.LAST_MONTH_END,
                        },
                        {
                            "projectCode": TEST_PROJECT_CODE,
                            "definitionCode": TEST_DEFINITION_CODE,
                            "depTaskCode": TEST_TASK_CODE,
                            "cycle": "week",
                            "dateValue": DependentDate.LAST_WEEK,
                        },
                    ],
                }
                for op in TEST_OPERATOR_LIST
            ],
        ),
        # Test dependent operator (And | Or) with multiply dependent item
        (
            (And, Or),
            (
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_MONTH_END,
                },
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_WEEK,
                },
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_ONE_DAYS,
                },
            ),
            [
                {
                    "relation": op,
                    "dependItemList": [
                        {
                            "projectCode": TEST_PROJECT_CODE,
                            "definitionCode": TEST_DEFINITION_CODE,
                            "depTaskCode": TEST_TASK_CODE,
                            "cycle": "month",
                            "dateValue": DependentDate.LAST_MONTH_END,
                        },
                        {
                            "projectCode": TEST_PROJECT_CODE,
                            "definitionCode": TEST_DEFINITION_CODE,
                            "depTaskCode": TEST_TASK_CODE,
                            "cycle": "week",
                            "dateValue": DependentDate.LAST_WEEK,
                        },
                        {
                            "projectCode": TEST_PROJECT_CODE,
                            "definitionCode": TEST_DEFINITION_CODE,
                            "depTaskCode": TEST_TASK_CODE,
                            "cycle": "day",
                            "dateValue": DependentDate.LAST_ONE_DAYS,
                        },
                    ],
                }
                for op in TEST_OPERATOR_LIST
            ],
        ),
    ],
)
@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": TEST_PROJECT_CODE,
        "processDefinitionCode": TEST_DEFINITION_CODE,
        "taskDefinitionCode": TEST_TASK_CODE,
    },
)
def test_operator_dependent_item(
    mock_code_info,
    operators: Tuple[DependentOperator],
    kwargs: Tuple[dict],
    expect: List[Dict],
):
    """Test DependentOperator(DependentItem) function get_define.

    Here we have test some cases as below, including single dependentItem and multiply dependentItem.
    ```py
    {
        "relation": "AND",
        "dependItemList": [
            {
                "projectCode": "project code",
                "definitionCode": "definition code",
                "depTaskCode": "dep task code",
                "cycle": "day",
                "dateValue": "today"
            },
            ...
        ]
    }
    ```
    """
    for idx, operator in enumerate(operators):
        # Use variable to keep one or more dependent item to test dependent operator behavior
        dependent_item_list = []
        for kwarg in kwargs:
            dependent_item = DependentItem(**kwarg)
            dependent_item_list.append(dependent_item)
        op = operator(*dependent_item_list)
        assert expect[idx] == op.get_define()


@pytest.mark.parametrize(
    # Test dependent operator, Test dependent item parameters, expect operator define
    "operators, args, expect",
    [
        # Test dependent operator (And | Or) with single dependent task list
        (
            (And, Or),
            (
                (And, Or),
                (
                    {
                        "project_name": TEST_PROJECT,
                        "process_definition_name": TEST_PROCESS_DEFINITION,
                        "dependent_task_name": TEST_TASK,
                        "dependent_date": DependentDate.LAST_MONTH_END,
                    },
                ),
            ),
            [
                {
                    "relation": par_op,
                    "dependTaskList": [
                        {
                            "relation": chr_op,
                            "dependItemList": [
                                {
                                    "projectCode": TEST_PROJECT_CODE,
                                    "definitionCode": TEST_DEFINITION_CODE,
                                    "depTaskCode": TEST_TASK_CODE,
                                    "cycle": "month",
                                    "dateValue": DependentDate.LAST_MONTH_END,
                                },
                            ],
                        }
                    ],
                }
                for (par_op, chr_op) in itertools.product(
                    TEST_OPERATOR_LIST, TEST_OPERATOR_LIST
                )
            ],
        ),
        # Test dependent operator (And | Or) with two dependent task list
        (
            (And, Or),
            (
                (And, Or),
                (
                    {
                        "project_name": TEST_PROJECT,
                        "process_definition_name": TEST_PROCESS_DEFINITION,
                        "dependent_task_name": TEST_TASK,
                        "dependent_date": DependentDate.LAST_MONTH_END,
                    },
                    {
                        "project_name": TEST_PROJECT,
                        "process_definition_name": TEST_PROCESS_DEFINITION,
                        "dependent_task_name": TEST_TASK,
                        "dependent_date": DependentDate.LAST_WEEK,
                    },
                ),
            ),
            [
                {
                    "relation": par_op,
                    "dependTaskList": [
                        {
                            "relation": chr_op,
                            "dependItemList": [
                                {
                                    "projectCode": TEST_PROJECT_CODE,
                                    "definitionCode": TEST_DEFINITION_CODE,
                                    "depTaskCode": TEST_TASK_CODE,
                                    "cycle": "month",
                                    "dateValue": DependentDate.LAST_MONTH_END,
                                },
                                {
                                    "projectCode": TEST_PROJECT_CODE,
                                    "definitionCode": TEST_DEFINITION_CODE,
                                    "depTaskCode": TEST_TASK_CODE,
                                    "cycle": "week",
                                    "dateValue": DependentDate.LAST_WEEK,
                                },
                            ],
                        }
                    ],
                }
                for (par_op, chr_op) in itertools.product(
                    TEST_OPERATOR_LIST, TEST_OPERATOR_LIST
                )
            ],
        ),
        # Test dependent operator (And | Or) with multiply dependent task list
        (
            (And, Or),
            (
                (And, Or),
                (
                    {
                        "project_name": TEST_PROJECT,
                        "process_definition_name": TEST_PROCESS_DEFINITION,
                        "dependent_task_name": TEST_TASK,
                        "dependent_date": DependentDate.LAST_MONTH_END,
                    },
                    {
                        "project_name": TEST_PROJECT,
                        "process_definition_name": TEST_PROCESS_DEFINITION,
                        "dependent_task_name": TEST_TASK,
                        "dependent_date": DependentDate.LAST_WEEK,
                    },
                    {
                        "project_name": TEST_PROJECT,
                        "process_definition_name": TEST_PROCESS_DEFINITION,
                        "dependent_task_name": TEST_TASK,
                        "dependent_date": DependentDate.LAST_ONE_DAYS,
                    },
                ),
            ),
            [
                {
                    "relation": par_op,
                    "dependTaskList": [
                        {
                            "relation": chr_op,
                            "dependItemList": [
                                {
                                    "projectCode": TEST_PROJECT_CODE,
                                    "definitionCode": TEST_DEFINITION_CODE,
                                    "depTaskCode": TEST_TASK_CODE,
                                    "cycle": "month",
                                    "dateValue": DependentDate.LAST_MONTH_END,
                                },
                                {
                                    "projectCode": TEST_PROJECT_CODE,
                                    "definitionCode": TEST_DEFINITION_CODE,
                                    "depTaskCode": TEST_TASK_CODE,
                                    "cycle": "week",
                                    "dateValue": DependentDate.LAST_WEEK,
                                },
                                {
                                    "projectCode": TEST_PROJECT_CODE,
                                    "definitionCode": TEST_DEFINITION_CODE,
                                    "depTaskCode": TEST_TASK_CODE,
                                    "cycle": "day",
                                    "dateValue": DependentDate.LAST_ONE_DAYS,
                                },
                            ],
                        }
                    ],
                }
                for (par_op, chr_op) in itertools.product(
                    TEST_OPERATOR_LIST, TEST_OPERATOR_LIST
                )
            ],
        ),
    ],
)
@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": TEST_PROJECT_CODE,
        "processDefinitionCode": TEST_DEFINITION_CODE,
        "taskDefinitionCode": TEST_TASK_CODE,
    },
)
def test_operator_dependent_task_list_multi_dependent_item(
    mock_code_info,
    operators: Tuple[DependentOperator],
    args: Tuple[Union[Tuple, dict]],
    expect: List[Dict],
):
    """Test DependentOperator(DependentOperator(DependentItem)) single operator function get_define.

    Here we have test some cases as below. This test case only test single DependTaskList with one or
    multiply dependItemList.
    ```py
    {
        "relation": "OR",
        "dependTaskList": [
            {
                "relation": "AND",
                "dependItemList": [
                    {
                        "projectCode": "project code",
                        "definitionCode": "definition code",
                        "depTaskCode": "dep task code",
                        "cycle": "day",
                        "dateValue": "today"
                    },
                    ...
                ]
            },
        ]
    }
    ```
    """
    # variable expect_idx record idx should be use to get specific expect
    expect_idx = 0

    for op_idx, operator in enumerate(operators):
        dependent_operator = args[0]
        dependent_item_kwargs = args[1]

        for dop_idx, dpt_op in enumerate(dependent_operator):
            dependent_item_list = []
            for dpt_kwargs in dependent_item_kwargs:
                dpti = DependentItem(**dpt_kwargs)
                dependent_item_list.append(dpti)
            child_dep_op = dpt_op(*dependent_item_list)
            op = operator(child_dep_op)
            assert expect[expect_idx] == op.get_define()
            expect_idx += 1


def get_dep_task_list(*operator):
    """Return dependent task list from given operators list."""
    result = []
    for op in operator:
        result.append(
            {
                "relation": op.operator_name(),
                "dependItemList": [
                    {
                        "projectCode": TEST_PROJECT_CODE,
                        "definitionCode": TEST_DEFINITION_CODE,
                        "depTaskCode": TEST_TASK_CODE,
                        "cycle": "month",
                        "dateValue": DependentDate.LAST_MONTH_END,
                    },
                ],
            }
        )
    return result


@pytest.mark.parametrize(
    # Test dependent operator, Test dependent item parameters, expect operator define
    "operators, args, expect",
    [
        # Test dependent operator (And | Or) with two dependent task list
        (
            (And, Or),
            (
                ((And, And), (And, Or), (Or, And), (Or, Or)),
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_MONTH_END,
                },
            ),
            [
                {
                    "relation": parent_op.operator_name(),
                    "dependTaskList": get_dep_task_list(*child_ops),
                }
                for parent_op in (And, Or)
                for child_ops in ((And, And), (And, Or), (Or, And), (Or, Or))
            ],
        ),
        # Test dependent operator (And | Or) with multiple dependent task list
        (
            (And, Or),
            (
                ((And, And, And), (And, And, And, And), (And, And, And, And, And)),
                {
                    "project_name": TEST_PROJECT,
                    "process_definition_name": TEST_PROCESS_DEFINITION,
                    "dependent_task_name": TEST_TASK,
                    "dependent_date": DependentDate.LAST_MONTH_END,
                },
            ),
            [
                {
                    "relation": parent_op.operator_name(),
                    "dependTaskList": get_dep_task_list(*child_ops),
                }
                for parent_op in (And, Or)
                for child_ops in (
                    (And, And, And),
                    (And, And, And, And),
                    (And, And, And, And, And),
                )
            ],
        ),
    ],
)
@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": TEST_PROJECT_CODE,
        "processDefinitionCode": TEST_DEFINITION_CODE,
        "taskDefinitionCode": TEST_TASK_CODE,
    },
)
def test_operator_dependent_task_list_multi_dependent_list(
    mock_code_info,
    operators: Tuple[DependentOperator],
    args: Tuple[Union[Tuple, dict]],
    expect: List[Dict],
):
    """Test DependentOperator(DependentOperator(DependentItem)) multiply operator function get_define.

    Here we have test some cases as below. This test case only test single DependTaskList with one or
    multiply dependTaskList.
    ```py
    {
        "relation": "OR",
        "dependTaskList": [
            {
                "relation": "AND",
                "dependItemList": [
                    {
                        "projectCode": "project code",
                        "definitionCode": "definition code",
                        "depTaskCode": "dep task code",
                        "cycle": "day",
                        "dateValue": "today"
                    }
                ]
            },
            ...
        ]
    }
    ```
    """
    # variable expect_idx record idx should be use to get specific expect
    expect_idx = 0
    for op_idx, operator in enumerate(operators):
        dependent_operator = args[0]
        dependent_item_kwargs = args[1]

        for dop_idx, dpt_ops in enumerate(dependent_operator):
            dependent_task_list = [
                dpt_op(DependentItem(**dependent_item_kwargs)) for dpt_op in dpt_ops
            ]
            op = operator(*dependent_task_list)
            assert (
                expect[expect_idx] == op.get_define()
            ), f"Failed with operator syntax {operator}.{dpt_ops}"
            expect_idx += 1


@patch(
    "pydolphinscheduler.tasks.dependent.DependentItem.get_code_from_gateway",
    return_value={
        "projectCode": TEST_PROJECT_CODE,
        "processDefinitionCode": TEST_DEFINITION_CODE,
        "taskDefinitionCode": TEST_TASK_CODE,
    },
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
def test_dependent_get_define(mock_code_version, mock_dep_code):
    """Test task dependent function get_define."""
    project_name = "test-dep-project"
    process_definition_name = "test-dep-definition"
    dependent_task_name = "test-dep-task"
    dep_operator = And(
        Or(
            # test dependence with add tasks
            DependentItem(
                project_name=project_name,
                process_definition_name=process_definition_name,
            )
        ),
        And(
            # test dependence with specific task
            DependentItem(
                project_name=project_name,
                process_definition_name=process_definition_name,
                dependent_task_name=dependent_task_name,
            )
        ),
    )

    name = "test_dependent_get_define"
    expect = {
        "code": 123,
        "name": name,
        "version": 1,
        "description": None,
        "delayTime": 0,
        "taskType": "DEPENDENT",
        "taskParams": {
            "resourceList": [],
            "localParams": [],
            "dependence": {
                "relation": "AND",
                "dependTaskList": [
                    {
                        "relation": "OR",
                        "dependItemList": [
                            {
                                "projectCode": TEST_PROJECT_CODE,
                                "definitionCode": TEST_DEFINITION_CODE,
                                "depTaskCode": "0",
                                "cycle": "day",
                                "dateValue": "today",
                            }
                        ],
                    },
                    {
                        "relation": "AND",
                        "dependItemList": [
                            {
                                "projectCode": TEST_PROJECT_CODE,
                                "definitionCode": TEST_DEFINITION_CODE,
                                "depTaskCode": TEST_TASK_CODE,
                                "cycle": "day",
                                "dateValue": "today",
                            }
                        ],
                    },
                ],
            },
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

    task = Dependent(name, dependence=dep_operator)
    assert task.get_define() == expect
