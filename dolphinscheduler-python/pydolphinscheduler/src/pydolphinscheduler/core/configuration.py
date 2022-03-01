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

import copy
import os
from pathlib import Path
from typing import Any, Dict

import yaml

from pydolphinscheduler.exceptions import PyDSConfException, PyDSParamException
from pydolphinscheduler.utils.path_dict import PathDict

DEFAULT_CONFIG_PATH = Path(__file__).resolve().parent.joinpath("default_config.yaml")


def get_config_file_path() -> Path:
    """Get the path of pydolphinscheduler configuration file."""
    pyds_home = os.environ.get("PYDOLPHINSCHEDULER_HOME", "~/pydolphinscheduler")
    config_file_path = Path(pyds_home).joinpath("config.yaml").expanduser()
    return config_file_path


def read_yaml(path: str) -> Dict:
    """Read configs dict from configuration file.

    :param path: The path of configuration file.
    """
    with open(path, "r") as f:
        return yaml.safe_load(f)


def write_yaml(context: Dict, path: str) -> None:
    """Write configs dict to configuration file.

    :param context: The configs dict write to configuration file.
    :param path: The path of configuration file.
    """
    parent = Path(path).parent
    if not parent.exists():
        parent.mkdir(parents=True)
    with open(path, mode="w") as f:
        f.write(yaml.dump(context))


def default_yaml_config() -> Dict:
    """Get default configs in ``DEFAULT_CONFIG_PATH``."""
    with open(DEFAULT_CONFIG_PATH, "r") as f:
        return yaml.safe_load(f)


def _whether_exists_config() -> bool:
    """Check whether config file already exists in :func:`get_config_file_path`."""
    return True if get_config_file_path().exists() else False


def get_all_configs():
    """Get all configs from configuration file."""
    exists = _whether_exists_config()
    if exists:
        return read_yaml(str(get_config_file_path()))
    else:
        return default_yaml_config()


# Add configs as module variables to avoid read configuration multiple times when
#  Get common configuration setting
#  Set or get multiple configs in single time
configs = get_all_configs()


def init_config_file() -> None:
    """Initialize configuration file to :func:`get_config_file_path`."""
    if _whether_exists_config():
        raise PyDSConfException(
            "Initialize configuration false to avoid overwrite configure by accident, file already exists "
            "in %s, if you wan to overwrite the exists configure please remove the exists file manually.",
            str(get_config_file_path()),
        )
    write_yaml(context=default_yaml_config(), path=str(get_config_file_path()))


def get_single_config(key: str) -> Any:
    """Get single config to configuration file.

    :param key: The config path want get.
    """
    global configs
    config_path_dict = PathDict(configs)
    if key not in config_path_dict:
        raise PyDSParamException(
            "Configuration path %s do not exists. Can not get configuration.", key
        )
    return config_path_dict.__getattr__(key)


def set_single_config(key: str, value: Any) -> None:
    """Change single config to configuration file.

    :param key: The config path want change.
    :param value: The new value want to set.
    """
    global configs
    config_path_dict = PathDict(configs)
    if key not in config_path_dict:
        raise PyDSParamException(
            "Configuration path %s do not exists. Can not set configuration.", key
        )
    config_path_dict.__setattr__(key, value)
    write_yaml(context=dict(config_path_dict), path=str(get_config_file_path()))


# Start Common Configuration Settings
path_configs = PathDict(copy.deepcopy(configs))

# Java Gateway Settings
JAVA_GATEWAY_ADDRESS = str(getattr(path_configs, "java_gateway.address"))
JAVA_GATEWAY_PORT = str(getattr(path_configs, "java_gateway.port"))
JAVA_GATEWAY_AUTO_CONVERT = str(getattr(path_configs, "java_gateway.auto_convert"))

# User Settings
USER_NAME = str(getattr(path_configs, "default.user.name"))
USER_PASSWORD = str(getattr(path_configs, "default.user.password"))
USER_EMAIL = str(getattr(path_configs, "default.user.email"))
USER_PHONE = str(getattr(path_configs, "default.user.phone"))
USER_STATE = str(getattr(path_configs, "default.user.state"))

# Workflow Settings
WORKFLOW_PROJECT = str(getattr(path_configs, "default.workflow.project"))
WORKFLOW_TENANT = str(getattr(path_configs, "default.workflow.tenant"))
WORKFLOW_USER = str(getattr(path_configs, "default.workflow.user"))
WORKFLOW_QUEUE = str(getattr(path_configs, "default.workflow.queue"))
WORKFLOW_WORKER_GROUP = str(getattr(path_configs, "default.workflow.worker_group"))
WORKFLOW_TIME_ZONE = str(getattr(path_configs, "default.workflow.time_zone"))

# End Common Configuration Setting
