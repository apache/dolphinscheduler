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

"""DolphinScheduler github resource plugin."""
import base64
from typing import Optional
from urllib.parse import urljoin

import requests

from pydolphinscheduler.constants import Symbol
from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.resources_plugin.base.git import Git, GitHubFileInfo


class GitHub(ResourcePlugin, Git):
    """GitHub resource plugin, a plugin for task and workflow to dolphinscheduler to read resource.

    :param prefix: A string representing the prefix of GitHub.
    :param access_token: A string used for identity authentication of GitHub private repository.
    """

    def __init__(
        self, prefix: str, access_token: Optional[str] = None, *args, **kwargs
    ):
        super().__init__(prefix, *args, **kwargs)
        self.access_token = access_token

    _git_file_info: Optional[GitHubFileInfo] = None

    def build_req_api(
        self,
        user: str,
        repo_name: str,
        file_path: str,
        api: str,
    ):
        """Build request file content API."""
        api = api.replace("{user}", user)
        api = api.replace("{repo_name}", repo_name)
        api = api.replace("{file_path}", file_path)
        return api

    def get_git_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        elements = path.split(Symbol.SLASH)
        index = self.get_index(path, Symbol.SLASH, 7)
        index = index + 1
        file_info = GitHubFileInfo(
            user=elements[3],
            repo_name=elements[4],
            branch=elements[6],
            file_path=path[index:],
        )
        self._git_file_info = file_info

    def get_req_url(self):
        """Build request URL according to file information."""
        return self.build_req_api(
            user=self._git_file_info.user,
            repo_name=self._git_file_info.repo_name,
            file_path=self._git_file_info.file_path,
            api="https://api.github.com/repos/{user}/{repo_name}/contents/{file_path}",
        )

    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = urljoin(self.prefix, suf)
        return self.req(path)

    def req(self, path: str):
        """Send HTTP request, parse response data, and get file content."""
        headers = {
            "Content-Type": "application/json; charset=utf-8",
        }
        if self.access_token is not None:
            headers.setdefault("Authorization", "Bearer %s" % self.access_token)
        self.get_git_file_info(path)
        response = requests.get(
            headers=headers,
            url=self.get_req_url(),
            params={"ref": self._git_file_info.branch},
        )
        if response.status_code == requests.codes.ok:
            json_response = response.json()
            content = base64.b64decode(json_response["content"])
            return content.decode("utf-8")
        else:
            raise Exception(response.json())
