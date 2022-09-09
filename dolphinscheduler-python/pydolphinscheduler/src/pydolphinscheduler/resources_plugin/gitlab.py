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
from urllib.parse import urljoin

import gitlab
import requests

from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin.base.git import (
    Git,
    GitLabFileInfo,
)


class GitLab(ResourcePlugin, Git):
    """GitLab object, declare GitLab resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of GitLab.
    :param private_token: A string used for identity authentication of GitLab private or Internal warehouse.
    :param oauth_token: A string used for identity authentication of GitLab private or Internal warehouse.
    :param username: A string representing the user of the warehouse.
    :param password: A string representing the user password.
    """

    # [start init_method]
    def __init__(
        self,
        prefix: str,
        private_token: Optional[str] = None,
        oauth_token: Optional[str] = None,
        username: Optional[str] = None,
        password: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(prefix, *args, **kwargs)
        self.private_token = private_token
        self.oauth_token = oauth_token
        self.username = username
        self.password = password

    # [end init_method]

    def get_git_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        elements = path.split("/")
        index = self.get_index(path, "/", 3)
        repo_name = None
        branch = None
        file_path = None
        user = None
        for i in range(0, len(elements)):
            if (
                i + 3 < len(elements)
                and elements[i + 1] == "-"
                and elements[i + 2] == "blob"
            ):
                repo_name = elements[i]
                user = "/".join(str(elements[j]) for j in range(3, i))
                branch = elements[i + 3]
                file_path = "/".join(
                    str(elements[j]) for j in range(i + 4, len(elements))
                )
                break
        if (
            repo_name is None
            or branch is None
            or file_path is None
            or file_path == ""
            or user is None
        ):
            raise PyResPluginException("Incomplete path.")

        self._git_file_info = GitLabFileInfo(
            host=path[0:index],
            repo_name=repo_name,
            branch=branch,
            file_path=file_path,
            api_version="v4",
            user=user,
        )

        """
        user=elements[3],
        repo_name=elements[4],
        branch=elements[6],
        file_path=path[index:],
        
        file_info = {
            "host": path[0:index],
            "repo_name": repo_name,
            "branch": branch,
            "file_path": file_path,
            "api_version": "v4",
            "user": user,
        }
        
        file_info = {
            "endpoint": endpoint,
            "bucket_name": bucket_name,
            "file_path": file_path,
        }
        
        "bucket": bucket,
        "key": key,
        
        """

    def authentication(self):
        """Gitlab authentication."""
        host = self._file_info["host"]
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
        host = self._file_info["host"]
        resp = requests.post("%s/oauth/token" % host, data=data)
        oauth_token = resp.json()["access_token"]
        return oauth_token

    # [start read_file_method]
    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = urljoin(self.prefix, suf)
        self.get_git_file_info(path)
        gl = self.authentication()
        project = gl.projects.get(
            "%s/%s" % (self._file_info["user"], self._file_info["repo_name"])
        )
        f = project.files.get(
            file_path=self._file_info["file_path"], ref=self._file_info["branch"]
        )
        file_content = f.decode()
        return file_content.decode()

    # [end read_file_method]
