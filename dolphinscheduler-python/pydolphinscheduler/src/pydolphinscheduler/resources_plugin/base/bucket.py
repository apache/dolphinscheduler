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

"""DolphinScheduler BucketFileInfo and Bucket object."""
from abc import ABCMeta, abstractmethod
from typing import Optional


class BucketFileInfo:
    """A class that defines the details of BUCKET files.

    :param bucket: A string representing the bucket to which the bucket file belongs.
    :param file_path: A string representing the bucket file path.
    """

    def __init__(
        self,
        bucket: Optional[str] = None,
        file_path: Optional[str] = None,
        *args,
        **kwargs
    ):
        self.bucket = bucket
        self.file_path = file_path


class OSSFileInfo(BucketFileInfo):
    """A class that defines the details of OSS files.

    :param endpoint: A string representing the OSS file endpoint.
    :param bucket: A string representing the bucket to which the OSS file belongs.
    :param file_path: A string representing the OSS file path.
    """

    def __init__(
        self,
        endpoint: Optional[str] = None,
        bucket: Optional[str] = None,
        file_path: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(bucket=bucket, file_path=file_path, *args, **kwargs)
        self.endpoint = endpoint


class S3FileInfo(BucketFileInfo):
    """A class that defines the details of S3 files.

    :param bucket: A string representing the bucket to which the S3 file belongs.
    :param file_path: A string representing the S3 file path.
    """

    def __init__(
        self,
        bucket: Optional[str] = None,
        file_path: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(bucket=bucket, file_path=file_path, *args, **kwargs)


class Bucket(object, metaclass=ABCMeta):
    """An abstract class of online code repository based on git implementation."""

    _bucket_file_info: Optional = None

    @abstractmethod
    def get_bucket_file_info(self, path: str):
        """Get the detailed information of BUCKET file according to the file URL."""
        raise NotImplementedError
