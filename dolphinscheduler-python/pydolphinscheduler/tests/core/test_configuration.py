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

import os
from pathlib import Path
from typing import Any

import pytest

from pydolphinscheduler.core import configuration
from pydolphinscheduler.core.configuration import (  set_single_config,
    BUILD_IN_CONFIG_PATH,
    get_single_config,
)
from pydolphinscheduler.exceptions import PyDSConfException
from pydolphinscheduler.utils.yaml_parser import YamlParser
from tests.testing.constants import DEV_MODE
from tests.testing.file import delete_file, get_file_content

env_pyds_home = "PYDOLPHINSCHEDULER_HOME"
config_file = "~/pydolphinscheduler/config.yaml"


@pytest.fixture
def teardown_del_file():
    """Teardown about delete default config file path."""
    yield
    delete_file(config_file)


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
        os.environ[env_pyds_home] = home
    assert Path(expect).expanduser() == configuration.config_path()


@pytest.mark.skipif(
    DEV_MODE,
    reason="Avoid delete ~/pydolphinscheduler/config.yaml by accident when test locally.",
)
def test_init_config_file(teardown_del_file):
    """Test init config file."""
    path = Path(config_file).expanduser()
    assert not path.exists()
    configuration.init_config_file()
    assert path.exists()

    assert get_file_content(path) == get_file_content(BUILD_IN_CONFIG_PATH)


@pytest.mark.skipif(
    DEV_MODE,
    reason="Avoid delete ~/pydolphinscheduler/config.yaml by accident when test locally.",
)
def test_init_config_file_duplicate(teardown_del_file):
    """Test raise error with init config file which already exists."""
    path = Path(config_file).expanduser()
    assert not path.exists()
    configuration.init_config_file()
    assert path.exists()

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
    ],
)
def test_single_config_get_set(teardown_del_file, key: str, val: Any, new_val: Any):
    """Test function :func:`get_single_config` and :func:`set_single_config`."""
    assert val == get_single_config(key)
    set_single_config(key, new_val)
    assert new_val == get_single_config(key)


@pytest.mark.parametrize(
    "config_name, expect",
    [
        ("JAVA_GATEWAY_ADDRESS", "127.0.0.1"),
        ("JAVA_GATEWAY_PORT", 25333),
        ("JAVA_GATEWAY_AUTO_CONVERT", True),
        ("USER_NAME", "userPythonGateway"),
        ("USER_PASSWORD", "userPythonGateway"),
        ("USER_EMAIL", "userPythonGateway@dolphinscheduler.com"),
        ("USER_PHONE", 11111111111),
        ("USER_STATE", 1),
        ("WORKFLOW_PROJECT", "project-pydolphin"),
        ("WORKFLOW_TENANT", "tenant_pydolphin"),
        ("WORKFLOW_USER", "userPythonGateway"),
        ("WORKFLOW_QUEUE", "queuePythonGateway"),
        ("WORKFLOW_WORKER_GROUP", "default"),
        ("WORKFLOW_TIME_ZONE", "Asia/Shanghai"),
    ],
)
def test_get_configuration(config_name: str, expect: Any):
    """Test get exists attribute in :mod:`configuration`."""
    assert expect == getattr(configuration, config_name)
