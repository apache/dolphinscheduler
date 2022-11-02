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
        self.user = user
        self.repo_name = repo_name
        self.branch = branch
        self.file_path = file_path


class GitHubFileInfo(GitFileInfo):
    """A class that defines the details of GitHub files.

    :param user: A string representing the user the GitHub file belongs to.
    :param repo_name: A string representing the repository to which the GitHub file belongs.
    :param branch: A string representing the branch to which the GitHub file belongs.
    :param file_path: A string representing the GitHub file path.
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
        super().__init__(
            user=user,
            repo_name=repo_name,
            branch=branch,
            file_path=file_path,
            *args,
            **kwargs
        )


class GitLabFileInfo(GitFileInfo):
    """A class that defines the details of GitLab files.

    :param host: A string representing the domain name the GitLab file belongs to.
    :param user: A string representing the user the GitLab file belongs to.
    :param repo_name: A string representing the repository to which the GitLab file belongs.
    :param branch: A string representing the branch to which the GitHub file belongs.
    :param file_path: A string representing the GitHub file path.
    """

    def __init__(
        self,
        host: Optional[str] = None,
        user: Optional[str] = None,
        repo_name: Optional[str] = None,
        branch: Optional[str] = None,
        file_path: Optional[str] = None,
        *args,
        **kwargs
    ):
        super().__init__(
            user=user,
            repo_name=repo_name,
            branch=branch,
            file_path=file_path,
            *args,
            **kwargs
        )
        self.host = host


class Git(object, metaclass=ABCMeta):
    """An abstract class of online code repository based on git implementation."""

    _git_file_info: Optional = None

    @abstractmethod
    def get_git_file_info(self, path: str):
        """Get the detailed information of GIT file according to the file URL."""
        raise NotImplementedError
