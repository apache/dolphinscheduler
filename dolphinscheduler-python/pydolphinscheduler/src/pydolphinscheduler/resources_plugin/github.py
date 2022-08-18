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
import os
from urllib.parse import urljoin

from pydolphinscheduler.exceptions import PyResPluginException


def constructURL(user: str = "404", repo_name: str = "404", path_to_file: str = "404", url: str = "404"):
    url = url.replace("{user}", user)
    url = url.replace("{repo_name}", repo_name)
    url = url.replace("{path_to_file}", path_to_file)
    return url


import requests

user = 'xdu-chenrj'
repo_name = 'test-ds-res-plugin'
path_to_file = 'union.sh'
json_url = 'https://api.github.com/repos/{user}/{repo_name}/contents/{path_to_file}?re'
proxies = {'http': 'http://127.0.0.1:8889', 'https': 'http://127.0.0.1:8889'}

json_url = constructURL(user, repo_name, path_to_file, json_url)
print(json_url)
# json_url = "https://github.com/xdu-chenrj/test-ds-res-plugin/blob/main/union.sh"
response = requests.get(json_url, proxies=proxies)
# print(response)
jsonResponse = response.text
print(jsonResponse)
#
if response.status_code == requests.codes.ok:
    jsonResponse = response.json()
    content = base64.b64decode(jsonResponse['content'])
    jsonString = content.decode('utf-8')
    print(jsonString)
else:
    raise PyResPluginException('Content was not found.')


class Github:
    """Github object, declare local resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of Local.

    """

    # [start init_method]
    def __init__(self, prefix: str):
        self._prefix = prefix

    # [end init_method]

    @property
    def prefix(self):
        """Get the _prefix attribute."""
        return self._prefix

    # [start auth_method]
    def auth(self):
        """GitHub private warehouse certification"""
        pass

    # [start read_file_method]
    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = urljoin(self.prefix, suf)

        if not path.exists():
            raise PyResPluginException("{} is not found".format(str(path)))
        if not os.access(str(path), os.R_OK):
            raise PyResPluginException(
                "You don't have permission to access {}".format(self.prefix + suf)
            )
        with open(path, "r") as f:
            content = f.read()
        return content

    # [end read_file_method]
