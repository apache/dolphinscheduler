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

"""DolphinScheduler Base object."""

from typing import Dict, Optional

# from pydolphinscheduler.side.user import User
from pydolphinscheduler.utils.string import attr2camel


class Base:
    """DolphinScheduler Base object."""

    _KEY_ATTR: set = {"name", "description"}

    _TO_DICT_ATTR: set = set()

    DEFAULT_ATTR: Dict = {}

    def __init__(self, name: str, description: Optional[str] = None):
        self.name = name
        self.description = description

    def __repr__(self) -> str:
        return f'<{type(self).__name__}: name="{self.name}">'

    def __eq__(self, other):
        return type(self) == type(other) and all(
            getattr(self, a, None) == getattr(other, a, None) for a in self._KEY_ATTR
        )

    # TODO check how Redash do
    # TODO DRY
    def to_dict(self, camel_attr=True) -> Dict:
        """Get object key attribute dict.

        use attribute `self._TO_DICT_ATTR` to determine which attributes should including to
        children `to_dict` function.
        """
        # content = {}
        # for attr, value in self.__dict__.items():
        #     # Don't publish private variables
        #     if attr.startswith("_"):
        #         continue
        #     else:
        #         content[snake2camel(attr)] = value
        # content.update(self.DEFAULT_ATTR)
        # return content
        content = {}
        for attr in self._TO_DICT_ATTR:
            val = getattr(self, attr, None)
            if camel_attr:
                content[attr2camel(attr)] = val
            else:
                content[attr] = val
        return content
