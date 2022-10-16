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

import pytest

from pydolphinscheduler.cli.commands import cli
from tests.integration.test_tenant import get_tenant
from tests.testing.cli import CliTestWrapper


@pytest.mark.parametrize(
    "option, output",
    [
        (
            [
                "tenant",
                "--set",
                "test-tenant",
                "test-queue",
                "test-desc",
                "test-tenant-code",
                "admin",
            ],
            "Set tenant start.\n"
            "Tenant(name=test-tenant, description=test-desc, tenant_id=1, "
            "code=test-tenant-code, queue=test-queue, user_name=admin)\n"
            "Set tenant done.",
        ),
    ],
)
def test_tenant_setter(option, output):
    """Test subcommand `tenant` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success()
    assert (
        "Tenant(name=test-tenant, description=test-desc, tenant_id="
        in cli_test.result.output
    )


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["tenant", "--get", "test-name-1"],
            "Get tenant ('test-name-1',) from pydolphinscheduler.\n"
            "Tenant(name=tenant_pydolphin, description=None, tenant_id=None, "
            "code=None, queue=queuePythonGateway, user_name=None)",
        )
    ],
)
def test_tenant_getter(option, output):
    """Test subcommand `tenant` option `--getter`."""
    get_tenant()
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (
            ["tenant", "--update", "admin", "test-tenant-code", 1, "test-desc-1"],
            "Update tenant start.\n"
            "Tenant(name=tenant_pydolphin, description=test-desc-1, tenant_id=1, "
            "code=test-tenant-code, queue=1, user_name=None)\n"
            "Update tenant done.",
        ),
    ],
)
def test_tenant_updater(option, output):
    """Test subcommand `tenant` option `--updater`."""
    get_tenant(user_name="admin")
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success(output=output)


@pytest.mark.parametrize(
    "option, output",
    [
        (["tenant", "--delete", "admin", "abc"], None),
    ],
)
def test_tenant_deleter(option, output):
    """Test subcommand `tenant` option `--deleter`."""
    get_tenant(tenant_code="abc", user_name="admin")
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_success()
    assert "Delete tenant abc done." in cli_test.result.output
