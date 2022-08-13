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

"""Test process definition in integration."""

from typing import Dict

import pytest

from pydolphinscheduler.core.process_definition import ProcessDefinition
from pydolphinscheduler.tasks.shell import Shell

PROCESS_DEFINITION_NAME = "test_change_exists_attr_pd"
TASK_NAME = f"task_{PROCESS_DEFINITION_NAME}"


@pytest.mark.parametrize(
    "pre, post",
    [
        (
            {
                "user": "pre_user",
            },
            {
                "user": "post_user",
            },
        )
    ],
)
def test_change_process_definition_attr(pre: Dict, post: Dict):
    """Test whether process definition success when specific attribute change."""
    assert pre.keys() == post.keys(), "Not equal keys for pre and post attribute."
    for attrs in [pre, post]:
        with ProcessDefinition(name=PROCESS_DEFINITION_NAME, **attrs) as pd:
            Shell(name=TASK_NAME, command="echo 1")
            pd.submit()
