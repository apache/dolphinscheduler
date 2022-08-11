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

"""DolphinScheduler local resource plugin."""

import os
from pathlib import Path

from pydolphinscheduler.exceptions import PyResPluginException


class Local:
    # [init_method]
    def __init__(self, prefix: str):
        self._prefix = prefix

    # [end init_method]

    @property
    def prefix(self):
        """Get the _prefix attribute"""
        return self._prefix

    # [start read_file_method]
    def read_file(self, suf: str):
        """Get the content of the file, the address of the file is
        the prefix of the resource plugin plus the parameter suf
        """
        path = Path(self.prefix).joinpath(suf)
        if not path.exists():
            raise PyResPluginException("{} is not found".format(str(path)))
        if not os.access(str(path), os.R_OK):
            raise PyResPluginException("You don't have permission to access {}".format(self.prefix + suf))
        with open(path, 'r') as f:
            content = f.read()
        return content

    # [start read_file_method]
