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
from unittest.mock import PropertyMock, patch

import pytest

from pydolphinscheduler.resources_plugin import GitHub
from pydolphinscheduler.resources_plugin.base.git import GitFileInfo


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "user": "apache",
                "repo_name": "dolphinscheduler",
                "file_path": "script/install.sh",
                "api": "https://api.github.com/repos/{user}/{repo_name}/contents/{file_path}",
            },
            "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
        ),
    ],
)
def test_github_build_req_api(attr, expected):
    """Test the build_req_api function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    assert expected == github.build_req_api(**attr)


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "https://github.com/apache/dolphinscheduler/blob/dev/script/install.sh",
            {
                "user": "apache",
                "repo_name": "dolphinscheduler",
                "branch": "dev",
                "file_path": "script/install.sh",
            },
        ),
        (
            "https://github.com/apache/dolphinscheduler/blob/master/pom.xml",
            {
                "user": "apache",
                "repo_name": "dolphinscheduler",
                "branch": "master",
                "file_path": "pom.xml",
            },
        ),
        (
            "https://github.com/apache/dolphinscheduler/blob/1.3.9-release/docker/build/startup.sh",
            {
                "user": "apache",
                "repo_name": "dolphinscheduler",
                "branch": "1.3.9-release",
                "file_path": "docker/build/startup.sh",
            },
        ),
    ],
)
def test_github_get_git_file_info(attr, expected):
    """Test the get_git_file_info function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    github.get_git_file_info(attr)
    assert expected == github._git_file_info.__dict__


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            (
                {
                    "user": "apache",
                    "repo_name": "dolphinscheduler",
                    "file_path": "docker/build/startup.sh",
                }
            ),
            "https://api.github.com/repos/apache/dolphinscheduler/contents/docker/build/startup.sh",
        ),
        (
            (
                {
                    "user": "apache",
                    "repo_name": "dolphinscheduler",
                    "file_path": "pom.xml",
                }
            ),
            "https://api.github.com/repos/apache/dolphinscheduler/contents/pom.xml",
        ),
        (
            (
                {
                    "user": "apache",
                    "repo_name": "dolphinscheduler",
                    "file_path": "script/create-dolphinscheduler.sh",
                }
            ),
            "https://api.github.com/repos/apache/dolphinscheduler/contents/script/create-dolphinscheduler.sh",
        ),
    ],
)
@patch(
    "pydolphinscheduler.resources_plugin.github.GitHub._git_file_info",
    new_callable=PropertyMock,
)
def test_github_get_req_url(m_git_file_info, attr, expected):
    """Test the get_req_url function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    m_git_file_info.return_value = GitFileInfo(**attr)
    assert expected == github.get_req_url()


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "init": {"prefix": "prefix", "access_token": "access_token"},
                "file_path": "github_resource_plugin.sh",
                "file_content": "github resource plugin",
            },
            "github resource plugin",
        ),
        (
            {
                "init": {
                    "prefix": "prefix",
                },
                "file_path": "github_resource_plugin.sh",
                "file_content": "github resource plugin",
            },
            "github resource plugin",
        ),
    ],
)
@patch("pydolphinscheduler.resources_plugin.github.GitHub.req")
def test_github_read_file(m_req, attr, expected):
    """Test the read_file function of the github resource plug-in."""
    github = GitHub(**attr.get("init"))
    m_req.return_value = attr.get("file_content")
    assert expected == github.read_file(attr.get("file_path"))


@pytest.mark.skip(reason="Lack of test environment, need stable repository")
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "https://github.com/apache/dolphinscheduler/blob/dev/lombok.config",
            "#\n"
            "# Licensed to the Apache Software Foundation (ASF) under one or more\n"
            "# contributor license agreements.  See the NOTICE file distributed with\n"
            "# this work for additional information regarding copyright ownership.\n"
            "# The ASF licenses this file to You under the Apache License, Version 2.0\n"
            '# (the "License"); you may not use this file except in compliance with\n'
            "# the License.  You may obtain a copy of the License at\n"
            "#\n"
            "#     http://www.apache.org/licenses/LICENSE-2.0\n"
            "#\n"
            "# Unless required by applicable law or agreed to in writing, software\n"
            '# distributed under the License is distributed on an "AS IS" BASIS,\n'
            "# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.\n"
            "# See the License for the specific language governing permissions and\n"
            "# limitations under the License.\n"
            "#\n"
            "\n"
            "lombok.addLombokGeneratedAnnotation = true\n",
        ),
    ],
)
def test_github_req(attr, expected):
    """Test the req function of the github resource plug-in."""
    github = GitHub(
        prefix="prefix",
    )
    assert expected == github.req(attr)
