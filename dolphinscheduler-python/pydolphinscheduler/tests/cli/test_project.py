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

"""Test command line interface subcommand `project`."""
from unittest.mock import patch

import pytest

from pydolphinscheduler.cli.commands import cli
from pydolphinscheduler.models import Project
from tests.testing.cli import CliTestWrapper


def show_project(a=None, b=None, c=None, d=None, e=None, f=None, g=None):  # noqa: D103
    return "Project(name=test-name-1, description=test-description, code=1)"


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["project", "--set", "test-project", "test-desc", "admin"],
            "Set project start.\n"
            "Project(name=test-name-1, description=test-description, code=1)\n"
            "Set project done.",
        ),
    ],
)
@patch.object(Project, "create_if_not_exists", show_project)
@patch.object(Project, "__str__", show_project)
def test_project_setter(option, output):
    """Test subcommand `project` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["project", "--get", "admin", "test-name-1"],
            "Get project (('admin', 'test-name-1'),) from pydolphinscheduler.\n"
            "Project(name=test-name-1, description=test-description, code=1)",
        ),
    ],
)
@patch(
    "pydolphinscheduler.models.project.Project.get_project_by_name",
    return_value=Project(name="test-name-1", description="test-description", code=1),
)
def test_project_getter(mock_get_project_by_name, option, output):
    """Test subcommand `project` option `--getter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            [
                "project",
                "--update",
                "admin",
                0,
                "test-name-updated",
                "test-description-updated",
            ],
            "Update project start.\n"
            "Project(name=test-name-1, description=test-description, code=1)\n"
            "Update project done.",
        ),
    ],
)
@patch.object(Project, "update", show_project)
@patch.object(Project, "__str__", show_project)
def test_project_updater(option, output):
    """Test subcommand `project` option `--updater`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["project", "--delete", "admin", "test-name-1"],
            "Delete project (('admin', 'test-name-1'),) from pydolphinscheduler.\n"
            "Project(name=test-name-1, description=test-description, code=1)\n"
            "Delete project test-name-1 done.",
        ),
    ],
)
@patch(
    "pydolphinscheduler.models.project.Project.get_project_by_name",
    return_value=Project(name="test-name-1", description="test-description", code=1),
)
@patch.object(Project, "delete", show_project)
@patch.object(Project, "__str__", show_project)
def test_project_deleter(mock_get_project_by_name, option, output):
    """Test subcommand `project` option `--deleter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)
