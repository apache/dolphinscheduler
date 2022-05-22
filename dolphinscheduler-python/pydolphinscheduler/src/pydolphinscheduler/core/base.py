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

    # Object key attribute, to test whether object equals and so on.
    _KEY_ATTR: set = {"name", "description"}

    # Object defines attribute, use when needs to communicate with Java gateway server.
    _DEFINE_ATTR: set = set()

    # Object default attribute, will add those attribute to `_DEFINE_ATTR` when init assign missing.
    _DEFAULT_ATTR: Dict = {}

    def __init__(self, name: str, description: Optional[str] = None):
        self.name = name
        self.description = description

    def __repr__(self) -> str:
        return f'<{type(self).__name__}: name="{self.name}">'

    def __eq__(self, other):
        return type(self) == type(other) and all(
            getattr(self, a, None) == getattr(other, a, None) for a in self._KEY_ATTR
        )

    def get_define_custom(
        self, camel_attr: bool = True, custom_attr: set = None
    ) -> Dict:
        """Get object definition attribute by given attr set."""
        content = {}
        for attr in custom_attr:
            val = getattr(self, attr, None)
            if camel_attr:
                content[attr2camel(attr)] = val
            else:
                content[attr] = val
        return content

    def get_define(self, camel_attr: bool = True) -> Dict:
        """Get object definition attribute communicate to Java gateway server.

        use attribute `self._DEFINE_ATTR` to determine which attributes should including when
        object tries to communicate with Java gateway server.
        """
        content = self.get_define_custom(camel_attr, self._DEFINE_ATTR)
        update_default = {
            k: self._DEFAULT_ATTR.get(k) for k in self._DEFAULT_ATTR if k not in content
        }
        content.update(update_default)
        return content
