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

"""DolphinScheduler ResourcePlugin object."""

from abc import ABCMeta, abstractmethod

from pydolphinscheduler.exceptions import PyResPluginException


# [start resource_plugin_definition]
class ResourcePlugin(object, metaclass=ABCMeta):
    """ResourcePlugin object, declare resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of ResourcePlugin.

    """

    # [start init_method]
    def __init__(self, prefix: str, *args, **kwargs):
        self.prefix = prefix

    # [end init_method]

    # [start abstractmethod read_file]
    @abstractmethod
    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """

    # [end abstractmethod read_file]

    def get_index(self, s: str, x, n):
        """Find the subscript of the nth occurrence of the X character in the string s."""
        if n <= s.count(x):
            all_index = [key for key, value in enumerate(s) if value == x]
            return all_index[n - 1]
        else:
            raise PyResPluginException("Incomplete path.")


# [end resource_plugin_definition]
