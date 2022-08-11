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

"""Init resources_plugin package and dolphinScheduler ResourcePlugin object."""

import importlib
import importlib.util
from pathlib import Path
from typing import Generator, Any

from pydolphinscheduler.exceptions import PyDSConfException

path_resources_plugin = Path(__file__).parent


# [start resource_plugin_definition]
class ResourcePlugin:
    """ResourcePlugin object, declare resource plugin for task and workflow to dolphinscheduler.

        :param type: A unique, meaningful string for the ResourcePlugin,
        Its value should be taken from the constant of ResourceType in constants.py.
        :param prefix: A string representing the prefix of ResourcePlugin.

    """

    # [start init]
    def __init__(self, type: str, prefix: str):
        self.type = type
        self.prefix = prefix

    # [end init]

    # [start get_all_modules]
    def get_all_modules(self) -> Path:
        """Get all res files path in resources_plugin directory."""
        return (ex for ex in path_resources_plugin.iterdir() if ex.is_file() and not ex.name.startswith("__"))

    # [end get_all_modules]

    # [start import_module]
    def import_module(self, script_name, script_path):
        """Import module"""
        spec = importlib.util.spec_from_file_location(script_name, script_path)
        module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(module)
        plugin = getattr(module, self.type.capitalize())
        return plugin(self.prefix)

    # [end import_module]

    @property
    # [start resource]
    def resource(self):
        """Dynamically return resource plugin"""
        for ex in self.get_all_modules():
            if ex.stem == self.type:
                return self.import_module(ex.name, str(ex))
        raise PyDSConfException('{} type is not supported.'.format(self.type))
    # [end resource]
# [end resource_plugin_definition]
