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
from unittest.mock import patch

import pytest

from pydolphinscheduler.cli.commands import cli
from pydolphinscheduler.models import User
from tests.testing.cli import CliTestWrapper


def show_user(a=None, b=None, c=None, d=None, e=None, f=None, g=None):  # noqa: D103
    return (
        "User(user_id=1 name=test-name, email=test-email@abc.com, phone=17366637777, tenant=test-tenant, "
        "queue=test-queue, status=1)"
    )


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
            "User(user_id=1 name=test-name, email=test-email@abc.com, phone=17366637777, tenant=test-tenant, "
            "queue=test-queue, status=1)\n"
            "Set user done.",
        ),
    ],
)
@patch.object(User, "create_if_not_exists", show_user)
@patch.object(User, "__str__", show_user)
def test_user_set(option, output):
    """Test subcommand `user` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["user", "--get", 1],
            "Get user (1,) from pydolphinscheduler.\n"
            "User(user_id=1 name=test-name, email=test-email@abc.com, phone=17366637777, "
            "tenant=test-tenant, queue=test-queue, status=1)",
        )
    ],
)
@patch(
    "pydolphinscheduler.models.user.User.get_user",
    return_value=User(
        name="test-name",
        password="test-password",
        email="test-email@abc.com",
        phone="17366637777",
        tenant="test-tenant",
        queue="test-queue",
        status=1,
        user_id=1,
    ),
)
def test_user_getter(mock_get_user, option, output):
    """Test subcommand `user` option `--getter`."""
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
            "User(user_id=1 name=test-name, email=test-email@abc.com, phone=17366637777, tenant=test-tenant, "
            "queue=test-queue, status=1)\n"
            "Update user done.",
        )
    ],
)
@patch.object(User, "update", show_user)
@patch.object(User, "__str__", show_user)
def test_user_updater(option, output):
    """Test subcommand `user` option `--updater`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["user", "--delete", 1],
            "Delete user (1,) from pydolphinscheduler.\n" "Delete user 1 done.",
        )
    ],
)
@patch.object(User, "delete", show_user)
@patch.object(User, "get_user", User)
def test_user_deleter(option, output):
    """Test subcommand `user` option `--deleter`."""
    cli_test = CliTestWrapper(cli, option)
    print(cli_test.result.exception)
    cli_test.assert_success(output=output)
