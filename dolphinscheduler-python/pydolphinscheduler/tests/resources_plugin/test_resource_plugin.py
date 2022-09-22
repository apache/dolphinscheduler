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

"""Test abstract class resource_plugin."""

import pytest

from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin import GitHub


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "s": "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
                "x": "/",
                "n": 2,
            },
            7,
        ),
        (
            {
                "s": "https://api.github.com",
                "x": ":",
                "n": 1,
            },
            5,
        ),
    ],
)
def test_github_get_index(attr, expected):
    """Test the get_index function of the abstract class resource_plugin."""
    github = GitHub(prefix="prefix")
    assert expected == github.get_index(**attr)


@pytest.mark.parametrize(
    "attr",
    [
        {
            "s": "https://api.github.com",
            "x": "/",
            "n": 3,
        },
        {
            "s": "https://api.github.com/",
            "x": "/",
            "n": 4,
        },
    ],
)
def test_github_get_index_exception(attr):
    """Test exception to get_index function of abstract class resource_plugin."""
    with pytest.raises(
        PyResPluginException,
        match="Incomplete path.",
    ):
        github = GitHub(prefix="prefix")
        github.get_index(**attr)
