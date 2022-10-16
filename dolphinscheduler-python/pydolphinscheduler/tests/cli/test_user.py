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

"""Test command line interface subcommand `user`."""

import pytest

from pydolphinscheduler.cli.commands import cli
from tests.integration.test_user import get_user
from tests.testing.cli import CliTestWrapper


@pytest.mark.parametrize(
    "option, output",
    [
        (
            [
                "user",
                "--set",
                "test-name",
                "test-password",
                "test-email@abc.com",
                "17366637777",
                "test-tenant",
                "test-queue",
                1,
            ],
            "Set user start.\n"
            "User(user_id=2 name=test-name, email=test-email@abc.com, phone=17366637777, "
            "tenant=test-tenant, queue=test-queue, status=1)\nSet user done.",
        ),
    ],
)
def test_user_setter(option, output):
    """Test subcommand `user` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["user", "--get", 2],
            "Get user (2,) from pydolphinscheduler.\n"
            "User(user_id=2 name=test-name, email=test-email@abc.com, phone=17366637777, "
            "tenant=None, queue=None, status=1)",
        )
    ],
)
def test_user_getter(option, output):
    """Test subcommand `user` option `--getter`."""
    get_user()
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            [
                "user",
                "--update",
                "test-name",
                "test-password",
                "test-email@abc.com",
                "17366637766",
                "test-tenant",
                "test-queue",
                1,
            ],
            "Update user start.\n"
            "User(user_id=2 name=test-name, email=test-email@abc.com, phone=17366637766, "
            "tenant=tenant_pydolphin, queue=test-queue, status=1)\n"
            "Update user done.",
        )
    ],
)
def test_user_updater(option, output):
    """Test subcommand `user` option `--updater`."""
    get_user()
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["user", "--delete", 2],
            "Delete user (2,) from pydolphinscheduler.\n" "Delete user 2 done.",
        )
    ],
)
def test_user_deleter(option, output):
    """Test subcommand `user` option `--deleter`."""
    get_user()
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)
