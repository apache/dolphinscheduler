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

"""DolphinScheduler GitFileInfo and Git object."""

from abc import ABCMeta, abstractmethod
from typing import Optional


class GitFileInfo:
    """A class that defines the details of GIT files.

    :param user: A string representing the user the git file belongs to.
    :param repo_name: A string representing the repository to which the git file belongs.
    :param branch: A string representing the branch to which the git file belongs.
    :param file_path: A string representing the git file path.
    """

    def __init__(
        self,
        user: Optional[str] = None,
        repo_name: Optional[str] = None,
        branch: Optional[str] = None,
        file_path: Optional[str] = None,
        *args,
        **kwargs
    ):
        self._user = user
        self._repo_name = repo_name
        self._branch = branch
        self._file_path = file_path

    @property
    def user(self) -> str:
        """Get attribute user."""
        return self._user

    @property
    def repo_name(self) -> str:
        """Get attribute repo_name."""
        return self._repo_name

    @property
    def branch(self) -> str:
        """Get attribute branch."""
        return self._branch

    @property
    def file_path(self) -> str:
        """Get attribute file_path."""
        return self._file_path


# [start Git]
class Git(object, metaclass=ABCMeta):
    """An abstract class of online code warehouse based on git implementation."""

    _git_file_info: Optional[GitFileInfo] = None

    # [start abstractmethod git_file_info]
    @abstractmethod
    def get_git_file_info(self, path: str):
        """Get the detailed information of GIT file according to the file URL."""

    # [end abstractmethod git_file_info]


# [end Git]
