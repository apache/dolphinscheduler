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

"""Test Resource."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.core.resource import Resource


@pytest.mark.parametrize(
    "program_type, main_package, expect",
    [
        ("java", "WordCount.jar", {"id": 1, "name": "mock_resource"}),
        ("scala", "WordCount.jar", {"id": 1, "name": "mock_resource"}),
        ("python", "word_count.py", {"id": 1, "name": "mock_resource"}),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.resource.Resource.get_resource_info",
    return_value=({"id": 1, "name": "mock_resource"}),
)
def test_get_resource_detail(
    mock_resource, mock_code_version, program_type, main_package, expect
):
    """Test :func:`get_resource_info` can return expect value."""
    resource = Resource()
    resource_info = resource.get_resource_info(program_type, main_package)
    assert expect == resource_info
