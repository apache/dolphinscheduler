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

"""Test github resource plugin."""
import pytest

from pydolphinscheduler.resources_plugin.gitlab import GitLab


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "https://gitlab.com/pydolphinscheduler/ds-gitlab/-/blob/main/union.sh",
            {
                "branch": "main",
                "file_path": "union.sh",
                "host": "https://gitlab.com",
                "repo_name": "ds-gitlab",
                "user": "pydolphinscheduler",
            },
        ),
        (
            "https://gitlab.com/pydolphinscheduler/ds/-/blob/dev/test/exc.sh",
            {
                "branch": "dev",
                "file_path": "test/exc.sh",
                "host": "https://gitlab.com",
                "repo_name": "ds",
                "user": "pydolphinscheduler",
            },
        ),
    ],
)
def test_gitlab_get_git_file_info(attr, expected):
    """Test the get_file_info function of the gitlab resource plugin."""
    gitlab = GitLab(prefix="prefix")
    gitlab.get_git_file_info(attr)
    assert expected == gitlab._git_file_info.__dict__


@pytest.mark.skip(reason="This test needs gitlab service")
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "init": {
                    "prefix": "https://gitlab.com/pydolphinscheduler/ds-internal/-/blob/main",
                    "oauth_token": "24518bd4cf5bfe9xx",
                },
                "file_path": "union.sh",
            },
            "test gitlab resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://gitlab.com/pydolphinscheduler/ds/-/blob/main",
                    "private_token": "9TyTe2xx",
                },
                "file_path": "union.sh",
            },
            "test gitlab resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://gitlab.com/pydolphinscheduler/ds-gitlab/-/blob/main",
                    "username": "pydolphinscheduler",
                    "password": "4295xx",
                },
                "file_path": "union.sh",
            },
            "test gitlab resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://gitlab.com/pydolphinscheduler/ds-public/-/blob/main",
                },
                "file_path": "union.sh",
            },
            "test gitlab resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://gitlab.com/pydolphinscheduler/ds-internal/-/blob/main",
                    "username": "pydolphinscheduler",
                    "password": "429xxx",
                },
                "file_path": "union.sh",
            },
            "test gitlab resource plugin\n",
        ),
    ],
)
def test_gitlab_read_file(attr, expected):
    """Test the read_file function of the gitlab resource plug-in."""
    gitlab = GitLab(**attr.get("init"))
    assert expected == gitlab.read_file(attr.get("file_path"))
