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

"""Test module about function wrap task decorator."""

from unittest.mock import patch

import pytest

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.exceptions import PyDSParamException
from pydolphinscheduler.tasks.func_wrap import task
from tests.testing.decorator import foo as foo_decorator
from tests.testing.task import Task

PD_NAME = "test_process_definition"
TASK_NAME = "test_task"


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version", return_value=(12345, 1)
)
def test_single_task_outside(mock_code):
    """Test single decorator task which outside process definition."""

    @task
    def foo():
        print(TASK_NAME)

    with ProcessDefinition(PD_NAME) as pd:
        foo()

    assert pd is not None and pd.name == PD_NAME
    assert len(pd.tasks) == 1

    pd_task = pd.tasks[12345]
    assert pd_task.name == "foo"
    assert pd_task.raw_script == "def foo():\n    print(TASK_NAME)\nfoo()"


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version", return_value=(12345, 1)
)
def test_single_task_inside(mock_code):
    """Test single decorator task which inside process definition."""
    with ProcessDefinition(PD_NAME) as pd:

        @task
        def foo():
            print(TASK_NAME)

        foo()

    assert pd is not None and pd.name == PD_NAME
    assert len(pd.tasks) == 1

    pd_task = pd.tasks[12345]
    assert pd_task.name == "foo"
    assert pd_task.raw_script == "def foo():\n    print(TASK_NAME)\nfoo()"


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version", return_value=(12345, 1)
)
def test_addition_decorator_error(mock_code):
    """Test error when using task decorator to a function already have decorator."""

    @task
    @foo_decorator
    def foo():
        print(TASK_NAME)

    with ProcessDefinition(PD_NAME) as pd:  # noqa: F841
        with pytest.raises(
            PyDSParamException, match="Do no support other decorators for.*"
        ):
            foo()


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    side_effect=Task("test_func_wrap", "func_wrap").gen_code_and_version,
)
def test_multiple_tasks_outside(mock_code):
    """Test multiple decorator tasks which outside process definition."""

    @task
    def foo():
        print(TASK_NAME)

    @task
    def bar():
        print(TASK_NAME)

    with ProcessDefinition(PD_NAME) as pd:
        foo = foo()
        bar = bar()

        foo >> bar

    assert pd is not None and pd.name == PD_NAME
    assert len(pd.tasks) == 2

    task_foo = pd.get_one_task_by_name("foo")
    task_bar = pd.get_one_task_by_name("bar")
    assert set(pd.task_list) == {task_foo, task_bar}
    assert (
        task_foo is not None
        and task_foo._upstream_task_codes == set()
        and task_foo._downstream_task_codes.pop() == task_bar.code
    )
    assert (
        task_bar is not None
        and task_bar._upstream_task_codes.pop() == task_foo.code
        and task_bar._downstream_task_codes == set()
    )


@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    side_effect=Task("test_func_wrap", "func_wrap").gen_code_and_version,
)
def test_multiple_tasks_inside(mock_code):
    """Test multiple decorator tasks which inside process definition."""
    with ProcessDefinition(PD_NAME) as pd:

        @task
        def foo():
            print(TASK_NAME)

        @task
        def bar():
            print(TASK_NAME)

        foo = foo()
        bar = bar()

        foo >> bar

    assert pd is not None and pd.name == PD_NAME
    assert len(pd.tasks) == 2

    task_foo = pd.get_one_task_by_name("foo")
    task_bar = pd.get_one_task_by_name("bar")
    assert set(pd.task_list) == {task_foo, task_bar}
    assert (
        task_foo is not None
        and task_foo._upstream_task_codes == set()
        and task_foo._downstream_task_codes.pop() == task_bar.code
    )
    assert (
        task_bar is not None
        and task_bar._upstream_task_codes.pop() == task_foo.code
        and task_bar._downstream_task_codes == set()
    )
