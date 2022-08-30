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

from pydolphinscheduler.core.resource_plugin import ResourcePlugin

import boto3

from pydolphinscheduler.exceptions import PyResPluginException


class S3(ResourcePlugin):
    """S3 object, declare S3 resource plugin for task and workflow to dolphinscheduler.

    :param prefix: A string representing the prefix of S3.


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

    def get_file_info(self, path: str):
        """Get file information from the file url, like repository name, user, branch, and file path."""
        elements = path.split("/")
        if len(elements) < 4:
            raise PyResPluginException("Incomplete path.")
        """
        https://ds-resource-plugin.s3.amazonaws.com/dir/union.sh
        """
        bucket = elements[2].split(".")[0]
        key = "/".join(str(elements[i]) for i in range(3, len(elements)))
        file_info = {
            "bucket": bucket,
            "key": key,
        }
        return file_info

    # [start read_file_method]
    def read_file(self, suf: str):
        """Get the content of the file.

        The address of the file is the prefix of the resource plugin plus the parameter suf.
        """
        path = self.url_join(self.prefix, suf)
        file_info = self.get_file_info(path)
        bucket = file_info["bucket"]
        key = file_info["key"]
        s3_resource = boto3.resource('s3')
        s3_object = s3_resource.Object(bucket, key)
        return s3_object.get()['Body'].read().decode('utf-8')

    # [end read_file_method]
