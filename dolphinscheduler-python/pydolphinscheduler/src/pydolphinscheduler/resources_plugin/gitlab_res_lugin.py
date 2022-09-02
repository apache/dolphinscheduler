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


class GitLab(ResourcePlugin):
    """GitLab object, declare GitLab resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of GitLab.

    :param access_token: A string used for identity authentication of GitLab private warehouse.

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

    def get_index(self, s: str, x, n):
        """Find the subscript of the nth occurrence of the X character in the string s."""
        if n <= s.count(x):
            all_index = [key for key, value in enumerate(s) if value == x]
            return all_index[n - 1]
        else:
            return None

    def url_join(self, prefix: str, suf: str):
        """File url splicing."""
        if prefix[-1] != "/":
            prefix = prefix + "/"
        if suf[0] == "/":
            suf = suf[1:]
        return urljoin(prefix + "/", suf)

    def get_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        elements = path.split("/")
        index = self.get_index(path, "/", 3)
        if index is None:
            raise PyResPluginException("Incomplete path.")

        project_name = None
        branch = None
        file_path = None
        owner = None
        for i in range(0, len(elements)):
            if (
                i + 3 < len(elements)
                and elements[i + 1] == "-"
                and elements[i + 2] == "blob"
            ):
                project_name = elements[i]
                owner = "/".join(str(elements[j]) for j in range(3, i))
                branch = elements[i + 3]
                file_path = "/".join(
                    str(elements[j]) for j in range(i + 4, len(elements))
                )
                break

        if project_name is None or branch is None or file_path is None or file_path == "" or owner is None:
            raise PyResPluginException("Incomplete path.")

        file_info = {
            "host": path[0:index],
            "project_name": project_name,
            "branch": branch,
            "file_path": file_path,
            "api_version": "v4",
            "owner": owner,
        }
        self._file_info = file_info

    def authentication(self):
        host = self._file_info["host"]
        if self.private_token is not None:
            return gitlab.Gitlab(host, private_token=self.private_token)
        if self.oauth_token is not None:
            return gitlab.Gitlab(host, oauth_token=self.oauth_token)
        if self.username is not None and self.password is not None:
            oauth_token = self.OAuth_token()
            return gitlab.Gitlab(host, oauth_token=oauth_token)

    def OAuth_token(self):
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
        """Get the content of the file.a

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = self.url_join(self.prefix, suf)
        self.get_file_info(path)
        gl = self.authentication()
        project = gl.projects.get(
            self._file_info["owner"] + "/" + self._file_info["project_name"]
        )
        f = project.files.get(
            file_path=self._file_info["file_path"], ref=self._file_info["branch"]
        )
        file_content = f.decode()
        return file_content.decode()

    # [end read_file_method]
