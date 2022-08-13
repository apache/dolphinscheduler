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

"""Test example."""

import ast
import importlib
from unittest.mock import patch

import pytest

from tests.testing.constants import task_without_example
from tests.testing.path import get_all_examples, get_tasks
from tests.testing.task import Task

process_definition_name = set()


def import_module(script_name, script_path):
    """Import and run example module in examples directory."""
    spec = importlib.util.spec_from_file_location(script_name, script_path)
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


def test_task_without_example():
    """Test task which without example.

    Avoiding add new type of tasks but without adding example describe how to use it.
    """
    # We use example/tutorial.py as shell task example
    ignore_name = {"__init__.py", "shell.py", "func_wrap.py"}
    all_tasks = {task.stem for task in get_tasks(ignore_name=ignore_name)}

    have_example_tasks = set()
    start = "task_"
    end = "_example"
    for ex in get_all_examples():
        stem = ex.stem
        if stem.startswith(start) and stem.endswith(end):
            task_name = stem.replace(start, "").replace(end, "")
            have_example_tasks.add(task_name)

    assert all_tasks.difference(have_example_tasks) == task_without_example


@pytest.fixture
def setup_and_teardown_for_stuff():
    """Fixture of py.test handle setup and teardown."""
    yield
    global process_definition_name
    process_definition_name = set()


def submit_check_without_same_name(self):
    """Side effect for verifying process definition name and adding it to global variable."""
    if self.name in process_definition_name:
        raise ValueError(
            "Example process definition should not have same name, but get duplicate name: %s",
            self.name,
        )
    submit_add_process_definition(self)


def submit_add_process_definition(self):
    """Side effect for adding process definition name to global variable."""
    process_definition_name.add(self.name)


def test_example_basic():
    """Test example basic information.

    Which including:
    * File extension name is `.py`
    * All example except `tutorial.py` is end with keyword "_example"
    * All example must have not empty `__doc__`.
    """
    for ex in get_all_examples():
        # All files in example is python script
        assert (
            ex.suffix == ".py"
        ), f"We expect all examples is python script, but get {ex.name}."

        # All except tutorial and __init__ is end with keyword "_example"
        if (
            ex.stem
            not in ("tutorial", "tutorial_decorator", "tutorial_resource_plugin")
            and ex.stem != "__init__"
        ):
            assert ex.stem.endswith(
                "_example"
            ), f"We expect all examples script end with keyword '_example', but get {ex.stem}."

        # All files have __doc__
        tree = ast.parse(ex.read_text())
        example_doc = ast.get_docstring(tree, clean=False)
        assert (
            example_doc is not None
        ), f"We expect all examples have __doc__, but {ex.name} do not."


@patch("pydolphinscheduler.core.process_definition.ProcessDefinition.start")
@patch(
    "pydolphinscheduler.core.process_definition.ProcessDefinition.submit",
    side_effect=submit_check_without_same_name,
    autospec=True,
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    # Example bulk_create_example.py would create workflow dynamic by :func:`get_one_task_by_name`
    # and would raise error in :func:`get_one_task_by_name` if we return constant value
    # using :arg:`return_value`
    side_effect=Task("test_example", "test_example").gen_code_and_version,
)
def test_example_process_definition_without_same_name(
    mock_code_version, mock_submit, mock_start
):
    """Test all examples file without same process definition's name.

    Our process definition would compete with others if we have same process definition name. It will make
    different between actually workflow and our workflow-as-code file which make users feel strange.
    """
    for ex in get_all_examples():
        # We use side_effect `submit_check_without_same_name` overwrite :func:`submit`
        # and check whether it have duplicate name or not
        import_module(ex.name, str(ex))
    assert True


@patch("pydolphinscheduler.core.process_definition.ProcessDefinition.start")
@patch(
    "pydolphinscheduler.core.process_definition.ProcessDefinition.submit",
    side_effect=submit_add_process_definition,
    autospec=True,
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    # Example bulk_create_example.py would create workflow dynamic by :func:`get_one_task_by_name`
    # and would raise error in :func:`get_one_task_by_name` if we return constant value
    # using :arg:`return_value`
    side_effect=Task("test_example", "test_example").gen_code_and_version,
)
def test_file_name_in_process_definition(mock_code_version, mock_submit, mock_start):
    """Test example file name in example definition name.

    We should not directly assert equal, because some of the examples contain
    more than one process definition.
    """
    global process_definition_name
    for ex in get_all_examples():
        # Skip __init__ file
        if ex.stem == "__init__":
            continue
        # Skip bulk_create_example check, cause it contain multiple workflow and
        # without one named bulk_create_example
        if ex.stem == "bulk_create_example":
            continue
        process_definition_name = set()
        assert ex.stem not in process_definition_name
        import_module(ex.name, str(ex))
        assert ex.stem in process_definition_name
