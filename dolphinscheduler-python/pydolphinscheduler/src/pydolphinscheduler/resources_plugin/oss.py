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
from urllib.parse import urljoin, urlparse

import oss2

from pydolphinscheduler.constants import Symbol
from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.resources_plugin.base.bucket import Bucket, OSSFileInfo


class OSS(ResourcePlugin, Bucket):
    """OSS object, declare OSS resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of OSS.
    :param access_key_id: A string representing the ID of AccessKey for AliCloud OSS.
    :param access_key_secret: A string representing the secret of AccessKey for AliCloud OSS.
    """

    def __init__(
        self,
        prefix: str,
        access_key_id: Optional[str] = None,
        access_key_secret: Optional[str] = None,
        *args,
        **kwargs,
    ):
        super().__init__(prefix, *args, **kwargs)
        self.access_key_id = access_key_id
        self.access_key_secret = access_key_secret

    _bucket_file_info: Optional[OSSFileInfo] = None

    def get_bucket_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        self.get_index(path, Symbol.SLASH, 3)
        result = urlparse(path)
        hostname = result.hostname
        elements = hostname.split(Symbol.POINT)
        self._bucket_file_info = OSSFileInfo(
            endpoint=f"{result.scheme}://"
            f"{Symbol.POINT.join(str(elements[i]) for i in range(1, len(elements)))}",
            bucket=hostname.split(Symbol.POINT)[0],
            file_path=result.path[1:],
        )

    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = urljoin(self.prefix, suf)
        self.get_bucket_file_info(path)
        auth = oss2.Auth(self.access_key_id, self.access_key_secret)
        bucket = oss2.Bucket(
            auth, self._bucket_file_info.endpoint, self._bucket_file_info.bucket
        )
        result = bucket.get_object(self._bucket_file_info.file_path).read().decode()
        return result.read().decode()
