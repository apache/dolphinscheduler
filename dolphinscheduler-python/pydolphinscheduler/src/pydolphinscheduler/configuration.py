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

"""Configuration module for pydolphinscheduler."""
import os
from pathlib import Path
from typing import Any

from pydolphinscheduler.exceptions import PyDSConfException
from pydolphinscheduler.utils import file
from pydolphinscheduler.utils.yaml_parser import YamlParser

BUILD_IN_CONFIG_PATH = Path(__file__).resolve().parent.joinpath("default_config.yaml")


def config_path() -> Path:
    """Get the path of pydolphinscheduler configuration file."""
    pyds_home = os.environ.get("PYDS_HOME", "~/pydolphinscheduler")
    config_file_path = Path(pyds_home).joinpath("config.yaml").expanduser()
    return config_file_path


def get_configs() -> YamlParser:
    """Get all configuration settings from configuration file.

    Will use custom configuration file first if it exists, otherwise default configuration file in
    default path.
    """
    path = str(config_path()) if config_path().exists() else BUILD_IN_CONFIG_PATH
    with open(path, mode="r") as f:
        return YamlParser(f.read())


def init_config_file() -> None:
    """Initialize configuration file by default configs."""
    if config_path().exists():
        raise PyDSConfException(
            "Initialize configuration false to avoid overwrite configure by accident, file already exists "
            "in %s, if you wan to overwrite the exists configure please remove the exists file manually.",
            str(config_path()),
        )
    file.write(content=str(get_configs()), to_path=str(config_path()))


def get_single_config(key: str) -> Any:
    """Get single config to configuration file.

    Support get from nested keys by delimiter ``.``.

    For example, yaml config as below:

    .. code-block:: yaml

        one:
          two1:
            three: value1
          two2: value2

    you could get ``value1`` and ``value2`` by nested path

    .. code-block:: python

        value1 = get_single_config("one.two1.three")
        value2 = get_single_config("one.two2")

    :param key: The config key want to get it value.
    """
    config = get_configs()
    if key not in config:
        raise PyDSConfException(
            "Configuration path %s do not exists. Can not get configuration.", key
        )
    return config[key]


def set_single_config(key: str, value: Any) -> None:
    """Change single config to configuration file.

    For example, yaml config as below:

    .. code-block:: yaml

        one:
          two1:
            three: value1
          two2: value2

    you could change ``value1`` to ``value3``, also change ``value2`` to ``value4`` by nested path assigned

    .. code-block:: python

        set_single_config["one.two1.three"] = "value3"
        set_single_config["one.two2"] = "value4"

    :param key: The config key want change.
    :param value: The new value want to set.
    """
    config = get_configs()
    if key not in config:
        raise PyDSConfException(
            "Configuration path %s do not exists. Can not set configuration.", key
        )
    config[key] = value
    file.write(content=str(config), to_path=str(config_path()), overwrite=True)


def get_int(val: Any) -> int:
    """Covert value to int."""
    return int(val)


def get_bool(val: Any) -> bool:
    """Covert value to boolean."""
    if isinstance(val, str):
        return val.lower() in {"true", "t"}
    elif isinstance(val, int):
        return val == 1
    else:
        return bool(val)


# Start Common Configuration Settings

# Add configs as module variables to avoid read configuration multiple times when
#  Get common configuration setting
#  Set or get multiple configs in single time
configs: YamlParser = get_configs()

# Java Gateway Settings
JAVA_GATEWAY_ADDRESS = os.environ.get(
    "PYDS_JAVA_GATEWAY_ADDRESS", configs.get("java_gateway.address")
)
JAVA_GATEWAY_PORT = get_int(
    os.environ.get("PYDS_JAVA_GATEWAY_PORT", configs.get("java_gateway.port"))
)
JAVA_GATEWAY_AUTO_CONVERT = get_bool(
    os.environ.get(
        "PYDS_JAVA_GATEWAY_AUTO_CONVERT", configs.get("java_gateway.auto_convert")
    )
)

# User Settings
USER_NAME = os.environ.get("PYDS_USER_NAME", configs.get("default.user.name"))
USER_PASSWORD = os.environ.get(
    "PYDS_USER_PASSWORD", configs.get("default.user.password")
)
USER_EMAIL = os.environ.get("PYDS_USER_EMAIL", configs.get("default.user.email"))
USER_PHONE = str(os.environ.get("PYDS_USER_PHONE", configs.get("default.user.phone")))
USER_STATE = get_int(
    os.environ.get("PYDS_USER_STATE", configs.get("default.user.state"))
)

# Workflow Settings
WORKFLOW_PROJECT = os.environ.get(
    "PYDS_WORKFLOW_PROJECT", configs.get("default.workflow.project")
)
WORKFLOW_TENANT = os.environ.get(
    "PYDS_WORKFLOW_TENANT", configs.get("default.workflow.tenant")
)
WORKFLOW_USER = os.environ.get(
    "PYDS_WORKFLOW_USER", configs.get("default.workflow.user")
)
WORKFLOW_QUEUE = os.environ.get(
    "PYDS_WORKFLOW_QUEUE", configs.get("default.workflow.queue")
)
WORKFLOW_RELEASE_STATE = os.environ.get(
    "PYDS_WORKFLOW_RELEASE_STATE", configs.get("default.workflow.release_state")
)
WORKFLOW_WORKER_GROUP = os.environ.get(
    "PYDS_WORKFLOW_WORKER_GROUP", configs.get("default.workflow.worker_group")
)
WORKFLOW_TIME_ZONE = os.environ.get(
    "PYDS_WORKFLOW_TIME_ZONE", configs.get("default.workflow.time_zone")
)
WORKFLOW_WARNING_TYPE = os.environ.get(
    "PYDS_WORKFLOW_WARNING_TYPE", configs.get("default.workflow.warning_type")
)

# End Common Configuration Setting
