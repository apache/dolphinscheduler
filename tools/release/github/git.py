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

"""Github utils for git operations."""

from pathlib import Path
from typing import Dict, Optional

from git import Repo

git_dir_path: Path = Path(__file__).parent.parent.parent.parent.joinpath(".git")


class Git:
    """Operator to handle git object.

    :param path: git repository path
    :param branch: branch you want to query
    """

    def __init__(
        self, path: Optional[str] = git_dir_path, branch: Optional[str] = None
    ):
        self.path = path
        self.branch = branch

    @property
    def repo(self) -> Repo:
        """Get git repo object."""
        return Repo(self.path)

    def has_commit_current(self, sha: str) -> bool:
        """Whether SHA in current branch."""
        branches = self.repo.git.branch("--contains", sha)
        return f"* {self.repo.active_branch.name}" in branches

    def has_commit_global(self, sha: str) -> bool:
        """Whether SHA in all branches."""
        try:
            self.repo.commit(sha)
            return True
        except ValueError:
            return False

    def cherry_pick_pr(self, pr: Dict) -> None:
        """Run command `git cherry-pick -x <SHA>`."""
        sha = pr["merge_commit_sha"]
        if not self.has_commit_global(sha):
            raise RuntimeError(
                "Cherry-pick SHA %s error because SHA not exists,"
                "please make sure you local default branch is up-to-date",
                sha,
            )
        if self.has_commit_current(sha):
            print("SHA %s already in current active branch, skip it.", sha)
        self.repo.git.cherry_pick("-x", sha)
