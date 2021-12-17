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

"""Test Database."""


from unittest.mock import patch

import pytest

from pydolphinscheduler.core.database import Database

TEST_DATABASE_DATASOURCE_NAME = "test_datasource"
TEST_DATABASE_TYPE_KEY = "type"
TEST_DATABASE_KEY = "datasource"


@pytest.mark.parametrize(
    "expect",
    [
        {
            TEST_DATABASE_TYPE_KEY: "mock_type",
            TEST_DATABASE_KEY: 1,
        }
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.database.Database.get_database_info",
    return_value=({"id": 1, "type": "mock_type"}),
)
def test_get_datasource_detail(mock_datasource, mock_code_version, expect):
    """Test :func:`get_database_type` and :func:`get_database_id` can return expect value."""
    database_info = Database(
        TEST_DATABASE_DATASOURCE_NAME, TEST_DATABASE_TYPE_KEY, TEST_DATABASE_KEY
    )
    assert expect == database_info
