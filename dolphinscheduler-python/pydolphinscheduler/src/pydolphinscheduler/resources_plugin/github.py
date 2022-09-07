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

from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.exceptions import PyResPluginException


class GitHub(ResourcePlugin):
    """GitHub object, declare GitHub resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of GitHub.

    :param access_token: A string used for identity authentication of GitHub private warehouse.

    :param username: A string representing the user of the warehouse.

    :param password: A string representing the user password, it is equal to access_token.


    """

    # [start init_method]
    def __init__(
        self,
        prefix: str,
        access_token: Optional[str] = None,
        username: Optional[str] = None,
        password: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(prefix, *args, **kwargs)
        self.access_token = access_token
        self.username = username
        self.password = password

    # [end init_method]

    _req_api = "https://api.github.com/repos/{user}/{repo_name}/contents/{file_path}"

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

    def url_join(self, prefix: str, suf: str):
        """File url splicing."""
        if prefix[-1] != "/":
            prefix = prefix + "/"
        if suf[0] == "/":
            suf = suf[1:]
        return urljoin(prefix + "/", suf)

    def get_index(self, s: str, x, n):
        """Find the subscript of the nth occurrence of the X character in the string s."""
        if n <= s.count(x):
            all_index = [key for key, value in enumerate(s) if value == x]
            return all_index[n - 1]
        else:
            return None

    def get_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        elements = path.split("/")
        index = self.get_index(path, "/", 7)
        if index is None:
            raise PyResPluginException("Incomplete path.")
        index = index + 1
        file_info = {
            "user": elements[3],
            "repo_name": elements[4],
            "branch": elements[6],
            "file_path": path[index:],
        }
        return file_info

    def get_req_url(self, file_info: dict):
        """Build request URL according to file information."""
        user = file_info["user"]
        repo_name = file_info["repo_name"]
        file_path = file_info["file_path"]
        url = self.build_req_api(user, repo_name, file_path, self._req_api)
        return url

    # [start read_file_method]
    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = self.url_join(self.prefix, suf)
        return self.req(path)

    # [end read_file_method]

    def req(self, path: str):
        """Send HTTP request, parse response data, and get file content."""
        headers = {
            "user-agent": "Mozilla/5.0 (X11; Linux x86_64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/104.0.0.0 Safari/537.36",
            "Content-Type": "application/json; charset=utf-8",
        }
        if self.access_token is not None:
            headers.setdefault("Authorization", "Bearer %s" % self.access_token)
        if self.username is not None and self.password is not None:
            base64string = base64.b64encode(
                bytes("%s:%s" % (self.username, self.password), encoding="utf-8")
            )
            headers.setdefault(
                "Authorization", "Basic %s" % base64string.decode("utf-8")
            )

        file_info = self.get_file_info(path)
        url = self.get_req_url(file_info)
        params = {"ref": file_info["branch"]}
        response = requests.get(
            headers=headers,
            url=url,
            params=params,
        )
        if response.status_code == requests.codes.ok:
            json_response = response.json()
            content = base64.b64decode(json_response["content"])
            return content.decode("utf-8")
        else:
            if response.status_code == requests.codes.not_found:
                raise PyResPluginException("{} is not found.".format(path))
            if response.status_code == requests.codes.unauthorized:
                raise PyResPluginException("unauthorized.")
            raise PyResPluginException("Unknown exception.")
