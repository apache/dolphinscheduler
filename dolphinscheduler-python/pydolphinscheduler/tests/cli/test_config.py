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

"""Test command line interface subcommand `config`."""

import os
from pathlib import Path

import pytest

from pydolphinscheduler.cli.commands import cli
from pydolphinscheduler.core.configuration import config_path
from tests.testing.cli import CliTestWrapper
from tests.testing.constants import DEV_MODE

config_file = "config.yaml"


@pytest.fixture
def delete_tmp_config_file():
    """Util for deleting temp configuration file after test finish."""
    yield
    config_file_path = config_path()
    config_file_path.unlink()


@pytest.mark.skipif(
    DEV_MODE,
    reason="Avoid delete ~/pydolphinscheduler/config.yaml by accident when test locally.",
)
@pytest.mark.parametrize(
    "home",
    [
        None,
        "/tmp/pydolphinscheduler",
        "/tmp/test_abc",
    ],
)
def test_config_init(delete_tmp_config_file, home):
    """Test command line interface `config --init`."""
    if home:
        os.environ["PYDOLPHINSCHEDULER_HOME"] = home
        config_path = home
    else:
        config_path = "~/pydolphinscheduler"

    path = Path(config_path).joinpath(config_file).expanduser()
    assert not path.exists()

    cli_test = CliTestWrapper(cli, ["config", "--init"])
    cli_test.assert_success()

    assert path.exists()
    with path.open(mode="r") as cli_crt, open(path, "r") as src:
        assert src.read() == cli_crt.read()


@pytest.mark.parametrize(
    "key, expect",
    [
        # We test each key in one single section
        ("java_gateway.address", "127.0.0.1"),
        ("default.user.name", "userPythonGateway"),
        ("default.workflow.project", "project-pydolphin"),
    ],
)
def test_config_get(delete_tmp_config_file, key: str, expect: str):
    """Test command line interface `config --get XXX`."""
    os.environ["PYDOLPHINSCHEDULER_HOME"] = "/tmp/pydolphinscheduler"
    cli_test = CliTestWrapper(cli, ["config", "--init"])
    cli_test.assert_success()

    cli_test = CliTestWrapper(cli, ["config", "--get", key])
    cli_test.assert_success(output=f"{key} = {expect}", fuzzy=True)


@pytest.mark.parametrize(
    "keys, expects",
    [
        # We test mix section keys
        (("java_gateway.address", "java_gateway.port"), ("127.0.0.1", "25333")),
        (
            ("java_gateway.auto_convert", "default.user.tenant"),
            ("True", "tenant_pydolphin"),
        ),
        (
            (
                "java_gateway.port",
                "default.user.state",
                "default.workflow.worker_group",
            ),
            ("25333", "1", "default"),
        ),
    ],
)
def test_config_get_multiple(delete_tmp_config_file, keys: str, expects: str):
    """Test command line interface `config --get KEY1 --get KEY2 ...`."""
    os.environ["PYDOLPHINSCHEDULER_HOME"] = "/tmp/pydolphinscheduler"
    cli_test = CliTestWrapper(cli, ["config", "--init"])
    cli_test.assert_success()

    get_args = ["config"]
    for key in keys:
        get_args.append("--get")
        get_args.append(key)
    cli_test = CliTestWrapper(cli, get_args)

    for idx, expect in enumerate(expects):
        cli_test.assert_success(output=f"{keys[idx]} = {expect}", fuzzy=True)


@pytest.mark.parametrize(
    "key, value",
    [
        # We test each key in one single section
        ("java_gateway.address", "127.1.1.1"),
        ("default.user.name", "editUserPythonGateway"),
        ("default.workflow.project", "edit-project-pydolphin"),
    ],
)
def test_config_set(delete_tmp_config_file, key: str, value: str):
    """Test command line interface `config --set KEY VALUE`."""
    path = "/tmp/pydolphinscheduler"
    assert not Path(path).joinpath(config_file).exists()
    os.environ["PYDOLPHINSCHEDULER_HOME"] = path
    cli_test = CliTestWrapper(cli, ["config", "--init"])
    cli_test.assert_success()

    # Make sure value do not exists first
    cli_test = CliTestWrapper(cli, ["config", "--get", key])
    assert f"{key} = {value}" not in cli_test.result.output

    cli_test = CliTestWrapper(cli, ["config", "--set", key, value])
    cli_test.assert_success()

    cli_test = CliTestWrapper(cli, ["config", "--get", key])
    assert f"{key} = {value}" in cli_test.result.output


@pytest.mark.parametrize(
    "keys, values",
    [
        # We test each key in mixture section
        (("java_gateway.address", "java_gateway.port"), ("127.1.1.1", "25444")),
        (
            ("java_gateway.auto_convert", "default.user.tenant"),
            ("False", "edit_tenant_pydolphin"),
        ),
        (
            (
                "java_gateway.port",
                "default.user.state",
                "default.workflow.worker_group",
            ),
            ("25555", "0", "not-default"),
        ),
    ],
)
def test_config_set_multiple(delete_tmp_config_file, keys: str, values: str):
    """Test command line interface `config --set KEY1 VAL1 --set KEY2 VAL2`."""
    path = "/tmp/pydolphinscheduler"
    assert not Path(path).joinpath(config_file).exists()
    os.environ["PYDOLPHINSCHEDULER_HOME"] = path
    cli_test = CliTestWrapper(cli, ["config", "--init"])
    cli_test.assert_success()

    set_args = ["config"]
    for idx, key in enumerate(keys):
        # Make sure values do not exists first
        cli_test = CliTestWrapper(cli, ["config", "--get", key])
        assert f"{key} = {values[idx]}" not in cli_test.result.output

        set_args.append("--set")
        set_args.append(key)
        set_args.append(values[idx])

    cli_test = CliTestWrapper(cli, set_args)
    cli_test.assert_success()

    for idx, key in enumerate(keys):
        # Make sure values exists after `config --set` run
        cli_test = CliTestWrapper(cli, ["config", "--get", key])
        assert f"{key} = {values[idx]}" in cli_test.result.output
