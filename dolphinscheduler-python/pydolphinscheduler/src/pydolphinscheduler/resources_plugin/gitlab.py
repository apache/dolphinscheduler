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

"""DolphinScheduler gitlab resource plugin."""
from typing import Optional
from urllib.parse import urljoin, urlparse

import gitlab
import requests

from pydolphinscheduler.constants import Symbol
from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.resources_plugin.base.git import Git, GitLabFileInfo


class GitLab(ResourcePlugin, Git):
    """GitLab object, declare GitLab resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of GitLab.
    :param private_token: A string used for identity authentication of GitLab private or Internal repository.
    :param oauth_token: A string used for identity authentication of GitLab private or Internal repository.
    :param username: A string representing the user of the repository.
    :param password: A string representing the user password.
    """

    def __init__(
        self,
        prefix: str,
        private_token: Optional[str] = None,
        oauth_token: Optional[str] = None,
        username: Optional[str] = None,
        password: Optional[str] = None,
        *args,
        **kwargs,
    ):
        super().__init__(prefix, *args, **kwargs)
        self.private_token = private_token
        self.oauth_token = oauth_token
        self.username = username
        self.password = password

    def get_git_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        self.get_index(path, Symbol.SLASH, 8)
        result = urlparse(path)
        elements = result.path.split(Symbol.SLASH)
        self._git_file_info = GitLabFileInfo(
            host=f"{result.scheme}://{result.hostname}",
            repo_name=elements[2],
            branch=elements[5],
            file_path=Symbol.SLASH.join(
                str(elements[i]) for i in range(6, len(elements))
            ),
            user=elements[1],
        )

    def authentication(self):
        """Gitlab authentication."""
        host = self._git_file_info.host
        if self.private_token is not None:
            return gitlab.Gitlab(host, private_token=self.private_token)
        if self.oauth_token is not None:
            return gitlab.Gitlab(host, oauth_token=self.oauth_token)
        if self.username is not None and self.password is not None:
            oauth_token = self.OAuth_token()
            return gitlab.Gitlab(host, oauth_token=oauth_token)
        return gitlab.Gitlab(host)

    def OAuth_token(self):
        """Obtain OAuth Token."""
        data = {
            "grant_type": "password",
            "username": self.username,
            "password": self.password,
        }
        host = self._git_file_info.host
        resp = requests.post("%s/oauth/token" % host, data=data)
        oauth_token = resp.json()["access_token"]
        return oauth_token

    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = urljoin(self.prefix, suf)
        self.get_git_file_info(path)
        gl = self.authentication()
        project = gl.projects.get(
            "%s/%s" % (self._git_file_info.user, self._git_file_info.repo_name)
        )
        return (
            project.files.get(
                file_path=self._git_file_info.file_path, ref=self._git_file_info.branch
            )
            .decode()
            .decode()
        )
