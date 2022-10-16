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

import pytest

from pydolphinscheduler.cli.commands import cli
from tests.integration.test_project import get_project
from tests.testing.cli import CliTestWrapper


@pytest.mark.parametrize(
    "option, output",
    [
        (["project", "--set", "test-project", "test-desc", "admin"], None),
    ],
)
def test_project_setter(option, output):
    """Test subcommand `project` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success()
    assert "Set project done." in cli_test.result.output


@pytest.mark.parametrize(
    "option, output",
    [
        (["project", "--get", "admin", "test-name-1"], None),
    ],
)
def test_project_getter(option, output):
    """Test subcommand `project` option `--getter`."""
    get_project()
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success()
    assert (
        "Project(name=test-name-1, description=test-description, code="
        in cli_test.result.output
    )


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
            None,
        ),
    ],
)
def test_project_updater(option, output):
    """Test subcommand `project` option `--updater`."""
    project = get_project()
    option[3] = project.code
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success()
    assert "Update project done." in cli_test.result.output


@pytest.mark.parametrize(
    "option, output",
    [
        (["project", "--delete", "admin", "test-name-1"], None),
    ],
)
def test_project_deleter(option, output):
    """Test subcommand `project` option `--deleter`."""
    get_project()
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success()
    assert (
        "Project(name=test-name-1, description=test-description, code="
        in cli_test.result.output
    )
