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

from pydolphinscheduler.resources_plugin.oss import OSS


@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            "https://ospp-ds-private.oss-cn-hangzhou.aliyuncs.com/a.sh",
            {
                "endpoint": "https://oss-cn-hangzhou.aliyuncs.com",
                "file_path": "a.sh",
                "bucket": "ospp-ds-private",
            },
        ),
        (
            "https://ospp-ds-public.oss-cn-hangzhou.aliyuncs.com/dir/a.sh",
            {
                "endpoint": "https://oss-cn-hangzhou.aliyuncs.com",
                "file_path": "dir/a.sh",
                "bucket": "ospp-ds-public",
            },
        ),
    ],
)
def test_oss_get_bucket_file_info(attr, expected):
    """Test the get_bucket_file_info function of the oss resource plugin."""
    oss = OSS(prefix="prefix")
    oss.get_bucket_file_info(attr)
    assert expected == oss._bucket_file_info.__dict__


@pytest.mark.skip(reason="This test requires OSS services")
@pytest.mark.parametrize(
    "attr, expected",
    [
        (
            {
                "init": {
                    "prefix": "https://ospp-ds-private.oss-cn-hangzhou.aliyuncs.com",
                    "access_key_id": "LTAI5tP25Mxx",
                    "access_key_secret": "cSur23Qbxx",
                },
                "file_path": "a.sh",
            },
            "test oss resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://ospp-ds-private.oss-cn-hangzhou.aliyuncs.com/dir/",
                    "access_key_id": "LTAxx",
                    "access_key_secret": "cSur23Qxx",
                },
                "file_path": "b.sh",
            },
            "test oss resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://ospp-ds-private.oss-cn-hangzhou.aliyuncs.com",
                },
                "file_path": "b.sh",
            },
            "test oss resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://ospp-ds-public.oss-cn-hangzhou.aliyuncs.com",
                },
                "file_path": "b.sh",
            },
            "test oss resource plugin\n",
        ),
        (
            {
                "init": {
                    "prefix": "https://ospp-ds-public.oss-cn-hangzhou.aliyuncs.com/dir/",
                    "access_key_id": "LTAIxx",
                    "access_key_secret": "cSurxx",
                },
                "file_path": "a.sh",
            },
            "test oss resource plugin\n",
        ),
    ],
)
def test_oss_read_file(attr, expected):
    """Test the read_file function of the oss resource plug-in."""
    oss = OSS(**attr.get("init"))
    assert expected == oss.read_file(attr.get("file_path"))
