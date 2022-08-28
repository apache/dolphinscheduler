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

"""DolphinScheduler oss resource plugin."""
from typing import Optional
from urllib.parse import urljoin

import oss2

from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.exceptions import PyResPluginException


class OSS(ResourcePlugin):
    """OSS object, declare OSS resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of GitLab.

    :param access_key_id: xxx.

    :param access_key_secret: xxx.

    """

    # [start init_method]
    def __init__(
        self,
        prefix: str,
        access_key_id: Optional[str] = None,
        access_key_secret: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(prefix, *args, **kwargs)
        self.access_key_id = access_key_id
        self.access_key_secret = access_key_secret

    # [end init_method]

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
        if len(elements) < 4:
            raise PyResPluginException("Incomplete path.")
        bucket_name = elements[2].split(".")[0]
        region = elements[2].split(".")[1]
        endpoint = "https://" + region + ".aliyuncs.com"
        file_path = "/".join(str(elements[i]) for i in range(3, len(elements)))
        file_info = {
            "endpoint": endpoint,
            "bucket_name": bucket_name,
            "file_path": file_path,
        }
        return file_info

    # [start read_file_method]
    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = self.url_join(self.prefix, suf)
        file_info = self.get_file_info(path)
        auth = oss2.Auth(self.access_key_id, self.access_key_secret)
        bucket = oss2.Bucket(auth, file_info["endpoint"], file_info["bucket_name"])
        result = bucket.get_object(file_info["file_path"])
        return result.read().decode()

    # [end read_file_method]
