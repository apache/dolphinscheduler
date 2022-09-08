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

from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin import GitHub


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
            {
                "prefix": "https://api.github.com/repos/apache/dolphinscheduler/contents/script/",
                "suf": "install.sh",
            },
            "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
        ),
        (
            {
                "prefix": "https://api.github.com/repos/apache/dolphinscheduler/contents/script",
                "suf": "/install.sh",
            },
            "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
        ),
        (
            {
                "prefix": "https://api.github.com/repos/apache/dolphinscheduler/contents/script/",
                "suf": "/install.sh",
            },
            "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
        ),
    ],
)
def test_github_url_join(attr, expected):
    """Test the url_join function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    assert expected == github.url_join(**attr)


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
                "s": "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
                "x": "/",
                "n": 3,
            },
            22,
        ),
        (
            {
                "s": "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
                "x": "/",
                "n": 9,
            },
            None,
        ),
        (
            {
                "s": "https://api.github.com/repos/apache/dolphinscheduler/contents/script/install.sh",
                "x": "/",
                "n": 10,
            },
            None,
        ),
    ],
)
def test_github_get_index(attr, expected):
    """Test the get_index function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    assert expected == github.get_index(**attr)


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
def test_github_get_file_info(attr, expected):
    """Test the get_file_info function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    assert expected == github.get_file_info(attr)


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
def test_github_get_req_url(attr, expected):
    """Test the get_req_url function of the github resource plug-in."""
    github = GitHub(prefix="prefix")
    assert expected == github.get_req_url(attr)

@pytest.mark.skip(
    reason="Lack of test environment, need stable repository"
)
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "lombok.config",
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
def test_github_read_file(attr, expected):
    """Test the read_file function of the github resource plug-in."""
    github = GitHub(
        prefix="https://github.com/apache/dolphinscheduler/blob/dev",
    )
    assert expected == github.read_file(attr)

@pytest.mark.skip(
    reason="Lack of test environment, need stable repository"
)
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


@pytest.mark.skip(
    reason="Lack of test environment, need stable personal repository and access_token"
)
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "prefix": "https://github.com/xdu-chenrj/test-ds-res-plugin/blob/main",
                "username": "xdu-chenrj",
                "password": "ghp_gxxx",
            },
            "test github resource plugin\n",
        ),
        (
            {
                "prefix": "https://github.com/xdu-chenrj/test-ds-res-plugin/blob/main",
                "access_token": "ghp_gxxxg",
            },
            "test github resource plugin\n",
        ),
    ],
)
def test_github_private_rep(attr, expected):
    """Test private warehouse file content acquisition."""
    github = GitHub(**attr)
    assert expected == github.read_file("union.sh")

@pytest.mark.skip(
    reason="Lack of test environment, need stable repository"
)
def test_github_file_not_found():
    """Test file does not exist."""
    with pytest.raises(
        PyResPluginException,
        match=".* is not found.",
    ):
        github = GitHub(prefix="https://github.com/apache/dolphinscheduler/blob/dev")
        github.read_file("a.sh")


@pytest.mark.skip(
    reason="Lack of test environment, need stable personal repository and access_token"
)
def test_github_unauthorized():
    """Test authentication exception of reading private warehouse file."""
    with pytest.raises(
        PyResPluginException,
        match="unauthorized.",
    ):
        github = GitHub(
            prefix="https://github.com/xdu-chenrj/test-ds-res-plugin/blob/main",
            access_token="test github resource plugin",
        )
        github.read_file("union.sh")
