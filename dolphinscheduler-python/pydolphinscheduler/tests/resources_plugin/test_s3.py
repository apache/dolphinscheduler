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

"""Test oss resource plugin."""
import pytest

from pydolphinscheduler.resources_plugin import S3


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "https://ds-resource-plugin-private.s3.amazonaws.com/a.sh",
            {
                "file_path": "a.sh",
                "bucket": "ds-resource-plugin-private",
            },
        ),
        (
            "https://ds-resource-plugin-public.s3.amazonaws.com/dir/a.sh",
            {
                "file_path": "dir/a.sh",
                "bucket": "ds-resource-plugin-public",
            },
        ),
    ],
)
def test_s3_get_bucket_file_info(attr, expected):
    """Test the get_bucket_file_info function of the s3 resource plugin."""
    s3 = S3(prefix="prefix")
    s3.get_bucket_file_info(attr)
    assert expected == s3._bucket_file_info.__dict__


@pytest.mark.skip(reason="This test requires s3 services")
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "init": {
                    "prefix": "https://ds-resource-plugin-private.s3.amazonaws.com/dir/",
                    "access_key_id": "LTAI5tP25Mxx",
                    "access_key_secret": "cSur23Qbxx",
                },
                "file_path": "a.sh",
            },
            "test s3 resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://ds-resource-plugin-public.s3.amazonaws.com/",
                },
                "file_path": "a.sh",
            },
            "test s3 resource plugin\n",
        ),
    ],
)
def test_s3_read_file(attr, expected):
    """Test the read_file function of the s3 resource plug-in."""
    s3 = S3(**attr.get("init"))
    assert expected == s3.read_file(attr.get("file_path"))
