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

"""Module for side object."""

from typing import Optional

from pydolphinscheduler.constants import ProcessDefinitionDefault
from pydolphinscheduler.core.base import Base


class BaseSide(Base):
    """Base class for side object, it declare base behavior for them."""

    def __init__(self, name: str, description: Optional[str] = None):
        super().__init__(name, description)

    @classmethod
    def create_if_not_exists(
        cls,
        # TODO comment for avoiding cycle import
        # user: Optional[User] = ProcessDefinitionDefault.USER
        user=ProcessDefinitionDefault.USER,
    ):
        """Create Base if not exists."""
        raise NotImplementedError
