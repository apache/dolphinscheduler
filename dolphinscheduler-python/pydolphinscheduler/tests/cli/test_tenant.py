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

"""Test command line interface subcommand `tenant`."""
from unittest.mock import patch

import pytest

from pydolphinscheduler.cli.commands import cli
from pydolphinscheduler.models import Tenant
from tests.testing.cli import CliTestWrapper


def show_tenant(a=None, b=None, c=None, d=None, e=None, f=None, g=None):  # noqa: D103
    return (
        "Tenant(name=test-tenant, description=test-desc, tenant_id=1, code=test-tenant-code, "
        "queue=test-queue, user_name=admin)"
    )


@pytest.mark.parametrize(
    "option, output",
    [
        (
            [
                "tenant",
                "--set",
                "--name",
                "test-tenant",
                "--description",
                "test-queue",
                "--queue",
                "test-desc",
                "--tenant_code",
                "test-tenant-code",
                "--user_name",
                "admin",
            ],
            "Set tenant start.\n"
            "Tenant(name=test-tenant, description=test-desc, tenant_id=1, "
            "code=test-tenant-code, queue=test-queue, user_name=admin)\n"
            "Set tenant done.",
        ),
    ],
)
@patch.object(Tenant, "create_if_not_exists", show_tenant)
@patch.object(Tenant, "__str__", show_tenant)
def test_tenant_setter(option, output):
    """Test subcommand `tenant` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["tenant", "--get", "--tenant_code", "test-name-1"],
            "Get tenant test-name-1 from pydolphinscheduler.\n"
            "Tenant(name=test-tenant, description=test-desc, tenant_id=1, code=test-tenant-code, "
            "queue=test-queue, user_name=admin)",
        )
    ],
)
@patch.object(Tenant, "get_tenant", show_tenant)
@patch.object(Tenant, "__str__", show_tenant)
def test_tenant_getter(option, output):
    """Test subcommand `tenant` option `--getter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            [
                "tenant",
                "--update",
                "--user_name",
                "admin",
                "--tenant_code",
                "test-tenant-code",
                "--queue",
                1,
                "--description",
                "test-desc-1",
            ],
            "Update tenant start.\n"
            "Tenant(name=test-tenant, description=test-desc, tenant_id=1, code=test-tenant-code, "
            "queue=test-queue, user_name=admin)\n"
            "Update tenant done.",
        ),
    ],
)
@patch(
    "pydolphinscheduler.models.tenant.Tenant.get_tenant",
    return_value=Tenant(
        name="test-name",
        description="test-desc-1",
        tenant_id=1,
        code="test-tenant-code",
        queue="test-queue",
        user_name="admin",
    ),
)
@patch.object(Tenant, "update", show_tenant)
@patch.object(Tenant, "__str__", show_tenant)
def test_tenant_updater(mock_get_tenant, option, output):
    """Test subcommand `tenant` option `--updater`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["tenant", "--delete", "--user_name", "admin", "--tenant_code", "abc"],
            "Delete tenant abc from pydolphinscheduler.\n"
            "Tenant(name=test-tenant, description=test-desc, tenant_id=1, code=test-tenant-code, "
            "queue=test-queue, user_name=admin)\n"
            "Delete tenant abc done.",
        ),
    ],
)
@patch(
    "pydolphinscheduler.models.tenant.Tenant.get_tenant",
    return_value=Tenant(
        name="test-name",
        description="test-desc-1",
        tenant_id=1,
        code="test-tenant-code",
        queue="test-queue",
        user_name="admin",
    ),
)
@patch.object(Tenant, "delete", show_tenant)
@patch.object(Tenant, "__str__", show_tenant)
def test_tenant_deleter(mock_get_tenant, option, output):
    """Test subcommand `tenant` option `--deleter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)
