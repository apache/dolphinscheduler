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
from unittest import skip

import pytest

from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin import GitHub
from pydolphinscheduler.resources_plugin.gitlab_res_lugin import GitLab


# @skip(reason="This test needs gitlab service")
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "http://124.221.129.34:8088/chenruijie/ds-gitlab/-/blob/main/union.sh",
            {
                "host": "http://124.221.129.34:8088",
                "project_name": "ds-gitlab",
                "branch": "main",
                "file_path": "union.sh",
                "api_version": "v4",
                "owner": "chenruijie"
            },
        ),
        (
            "https://gitlab.com/chenruijie/ds/-/blob/dev/test/exc.sh",
            {
                "host": "https://gitlab.com",
                "project_name": "ds",
                "branch": "dev",
                "file_path": "test/exc.sh",
                "api_version": "v4",
                "owner": "chenruijie"
            },
        ),
    ],
)
def test_gitlab_get_file_info(attr, expected):
    """Test the build_req_api function of the gitlab resource plug-in."""
    gitlab = GitLab(prefix="prefix")
    gitlab.get_file_info(attr)
    assert expected == gitlab._file_info


# @skip(reason="This test needs gitlab service")
@pytest.mark.parametrize(
    "attr",
    [
        "http://124.221.129.34:8088/chenruijie/ds-gitlab/-/blob/main",
        "http://124.221.129.34:8088",
    ],
)
def test_gitlab_get_file_info_exception(attr):
    """Test the build_req_api function of the gitlab resource plug-in."""
    with pytest.raises(
        PyResPluginException,
        match="Incomplete path.",
    ):
        gitlab = GitLab(prefix="prefix")
        gitlab.get_file_info(attr)

