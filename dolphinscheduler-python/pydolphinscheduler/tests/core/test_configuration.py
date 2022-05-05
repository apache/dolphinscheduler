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

"""Test class :mod:`pydolphinscheduler.core.configuration`' method."""

import importlib
import os
from pathlib import Path
from typing import Any

import pytest

from pydolphinscheduler.core import configuration
from pydolphinscheduler.core.configuration import (
    BUILD_IN_CONFIG_PATH,
    config_path,
    get_single_config,
    set_single_config,
)
from pydolphinscheduler.exceptions import PyDSConfException
from pydolphinscheduler.utils.yaml_parser import YamlParser
from tests.testing.constants import DEV_MODE, ENV_PYDS_HOME
from tests.testing.file import get_file_content


@pytest.fixture
def teardown_file_env():
    """Util for deleting temp configuration file and pop env var after test finish."""
    yield
    config_file_path = config_path()
    if config_file_path.exists():
        config_file_path.unlink()
    os.environ.pop(ENV_PYDS_HOME, None)


@pytest.mark.parametrize(
    "val, expect",
    [
        ("1", 1),
        ("123", 123),
        ("4567", 4567),
        (b"1234", 1234),
    ],
)
def test_get_int(val: Any, expect: int):
    """Test function :func:`configuration.get_int`."""
    assert configuration.get_int(val) == expect


@pytest.mark.parametrize(
    "val",
    [
        "a",
        "1a",
        "1d2",
        "1723-",
    ],
)
def test_get_int_error(val: Any):
    """Test function :func:`configuration.get_int`."""
    with pytest.raises(ValueError):
        configuration.get_int(val)


@pytest.mark.parametrize(
    "val, expect",
    [
        ("t", True),
        ("true", True),
        (1, True),
        (True, True),
        ("f", False),
        ("false", False),
        (0, False),
        (123, False),
        ("abc", False),
        ("abc1", False),
        (False, False),
    ],
)
def test_get_bool(val: Any, expect: bool):
    """Test function :func:`configuration.get_bool`."""
    assert configuration.get_bool(val) == expect


@pytest.mark.parametrize(
    "home, expect",
    [
        (None, "~/pydolphinscheduler/config.yaml"),
        ("/tmp/pydolphinscheduler", "/tmp/pydolphinscheduler/config.yaml"),
        ("/tmp/test_abc", "/tmp/test_abc/config.yaml"),
    ],
)
def test_config_path(home: Any, expect: str):
    """Test function :func:`config_path`."""
    if home:
        os.environ[ENV_PYDS_HOME] = home
    assert Path(expect).expanduser() == configuration.config_path()


@pytest.mark.parametrize(
    "home",
    [
        None,
        "/tmp/pydolphinscheduler",
        "/tmp/test_abc",
    ],
)
def test_init_config_file(teardown_file_env, home: Any):
    """Test init config file."""
    if home:
        os.environ[ENV_PYDS_HOME] = home
    elif DEV_MODE:
        pytest.skip(
            "Avoid delete ~/pydolphinscheduler/config.yaml by accident when test locally."
        )
    assert not config_path().exists()
    configuration.init_config_file()
    assert config_path().exists()

    assert get_file_content(config_path()) == get_file_content(BUILD_IN_CONFIG_PATH)


@pytest.mark.parametrize(
    "home",
    [
        None,
        "/tmp/pydolphinscheduler",
        "/tmp/test_abc",
    ],
)
def test_init_config_file_duplicate(teardown_file_env, home: Any):
    """Test raise error with init config file which already exists."""
    if home:
        os.environ[ENV_PYDS_HOME] = home
    elif DEV_MODE:
        pytest.skip(
            "Avoid delete ~/pydolphinscheduler/config.yaml by accident when test locally."
        )
    assert not config_path().exists()
    configuration.init_config_file()
    assert config_path().exists()

    with pytest.raises(PyDSConfException, match=".*file already exists.*"):
        configuration.init_config_file()


def test_get_configs_build_in():
    """Test function :func:`get_configs` with build-in config file."""
    content = get_file_content(BUILD_IN_CONFIG_PATH)
    assert YamlParser(content).src_parser == configuration.get_configs().src_parser
    assert YamlParser(content).dict_parser == configuration.get_configs().dict_parser


@pytest.mark.parametrize(
    "key, val, new_val",
    [
        ("java_gateway.address", "127.0.0.1", "127.1.1.1"),
        ("java_gateway.port", 25333, 25555),
        ("java_gateway.auto_convert", True, False),
        ("default.user.name", "userPythonGateway", "editUserPythonGateway"),
        ("default.user.password", "userPythonGateway", "editUserPythonGateway"),
        (
            "default.user.email",
            "userPythonGateway@dolphinscheduler.com",
            "userPythonGateway@edit.com",
        ),
        ("default.user.phone", 11111111111, 22222222222),
        ("default.user.state", 1, 0),
        ("default.workflow.project", "project-pydolphin", "eidt-project-pydolphin"),
        ("default.workflow.tenant", "tenant_pydolphin", "edit_tenant_pydolphin"),
        ("default.workflow.user", "userPythonGateway", "editUserPythonGateway"),
        ("default.workflow.queue", "queuePythonGateway", "editQueuePythonGateway"),
        ("default.workflow.worker_group", "default", "specific"),
        ("default.workflow.time_zone", "Asia/Shanghai", "Asia/Beijing"),
        ("default.workflow.warning_type", "NONE", "ALL"),
    ],
)
def test_single_config_get_set(teardown_file_env, key: str, val: Any, new_val: Any):
    """Test function :func:`get_single_config` and :func:`set_single_config`."""
    assert val == get_single_config(key)
    set_single_config(key, new_val)
    assert new_val == get_single_config(key)


def test_single_config_get_set_not_exists_key():
    """Test function :func:`get_single_config` and :func:`set_single_config` error while key not exists."""
    not_exists_key = "i_am_not_exists_key"
    with pytest.raises(PyDSConfException, match=".*do not exists.*"):
        get_single_config(not_exists_key)
    with pytest.raises(PyDSConfException, match=".*do not exists.*"):
        set_single_config(not_exists_key, not_exists_key)


@pytest.mark.parametrize(
    "config_name, expect",
    [
        ("JAVA_GATEWAY_ADDRESS", "127.0.0.1"),
        ("JAVA_GATEWAY_PORT", 25333),
        ("JAVA_GATEWAY_AUTO_CONVERT", True),
        ("USER_NAME", "userPythonGateway"),
        ("USER_PASSWORD", "userPythonGateway"),
        ("USER_EMAIL", "userPythonGateway@dolphinscheduler.com"),
        ("USER_PHONE", "11111111111"),
        ("USER_STATE", 1),
        ("WORKFLOW_PROJECT", "project-pydolphin"),
        ("WORKFLOW_TENANT", "tenant_pydolphin"),
        ("WORKFLOW_USER", "userPythonGateway"),
        ("WORKFLOW_QUEUE", "queuePythonGateway"),
        ("WORKFLOW_WORKER_GROUP", "default"),
        ("WORKFLOW_TIME_ZONE", "Asia/Shanghai"),
        ("WORKFLOW_WARNING_TYPE", "NONE"),
    ],
)
def test_get_configuration(config_name: str, expect: Any):
    """Test get exists attribute in :mod:`configuration`."""
    assert expect == getattr(configuration, config_name)


@pytest.mark.parametrize(
    "config_name, src, dest",
    [
        ("JAVA_GATEWAY_ADDRESS", "127.0.0.1", "192.168.1.1"),
        ("JAVA_GATEWAY_PORT", 25333, 25334),
        ("JAVA_GATEWAY_AUTO_CONVERT", True, False),
        ("USER_NAME", "userPythonGateway", "envUserPythonGateway"),
        ("USER_PASSWORD", "userPythonGateway", "envUserPythonGateway"),
        (
            "USER_EMAIL",
            "userPythonGateway@dolphinscheduler.com",
            "userPythonGateway@dolphinscheduler.com",
        ),
        ("USER_PHONE", "11111111111", "22222222222"),
        ("USER_STATE", 1, 0),
        ("WORKFLOW_PROJECT", "project-pydolphin", "env-project-pydolphin"),
        ("WORKFLOW_TENANT", "tenant_pydolphin", "env-tenant_pydolphin"),
        ("WORKFLOW_USER", "userPythonGateway", "envUserPythonGateway"),
        ("WORKFLOW_QUEUE", "queuePythonGateway", "envQueuePythonGateway"),
        ("WORKFLOW_WORKER_GROUP", "default", "custom"),
        ("WORKFLOW_TIME_ZONE", "Asia/Shanghai", "America/Los_Angeles"),
        ("WORKFLOW_WARNING_TYPE", "NONE", "ALL"),
    ],
)
def test_get_configuration_env(config_name: str, src: Any, dest: Any):
    """Test get exists attribute from environment variable in :mod:`configuration`."""
    assert getattr(configuration, config_name) == src

    env_name = f"PYDS_{config_name}"
    os.environ[env_name] = str(dest)
    # reload module configuration to re-get config from environment.
    importlib.reload(configuration)
    assert getattr(configuration, config_name) == dest

    # pop and reload configuration to test whether this config equal to `src` value
    os.environ.pop(env_name, None)
    importlib.reload(configuration)
    assert getattr(configuration, config_name) == src
    assert env_name not in os.environ
