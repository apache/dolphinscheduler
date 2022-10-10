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

"""DolphinScheduler S3 resource plugin."""

from typing import Optional
from urllib.parse import urljoin

import boto3

from pydolphinscheduler.constants import Symbol
from pydolphinscheduler.core.resource_plugin import ResourcePlugin
from pydolphinscheduler.resources_plugin.base.bucket import Bucket, S3FileInfo


class S3(ResourcePlugin, Bucket):
    """S3 object, declare S3 resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of S3.
    :param access_key_id: A string representing the ID of AccessKey for Amazon S3.
    :param access_key_secret: A string representing the secret of AccessKey for Amazon S3.
    """

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

    _bucket_file_info: Optional[S3FileInfo] = None

    def get_bucket_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        elements = path.split(Symbol.SLASH)
        self.get_index(path, Symbol.SLASH, 3)
        self._bucket_file_info = S3FileInfo(
            bucket=elements[2].split(Symbol.POINT)[0],
            file_path=Symbol.SLASH.join(
                str(elements[i]) for i in range(3, len(elements))
            ),
        )

    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = urljoin(self.prefix, suf)
        self.get_bucket_file_info(path)
        bucket = self._bucket_file_info.bucket
        key = self._bucket_file_info.file_path
        s3_resource = boto3.resource("s3")
        s3_object = s3_resource.Object(bucket, key)
        return s3_object.get()["Body"].read().decode("utf-8")
