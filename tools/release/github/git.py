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

from typing import Optional, Iterator, Dict

from git import Repo, Commit
from pathlib import Path

from subprocess import Popen, PIPE, STDOUT

git_dir_path: Path = Path(__file__).parent.parent.parent.parent.joinpath(".git")


class Git:
    def __init__(self, path: Optional[str] = git_dir_path, branch: Optional[str] = None):
        self.path = path
        self.branch = branch

    @property
    def repo(self) -> Repo:
        return Repo(self.path)

    def commits(self) -> Iterator[Commit]:
        if self.branch:
            return self.repo.iter_commits(self.branch)
        return self.repo.iter_commits()

    def has_commit_current(self, sha: str) -> bool:
        branches = self.repo.git.branch('--contains', sha)
        return self.repo.active_branch in branches

    def has_commit_global(self, sha: str) -> bool:
        try:
            self.repo.commit(sha)
            return True
        except ValueError:
            return False

    def cherry_pick_pr(self, pr: Dict) -> None:
        sha = pr['merge_commit_sha']
        if not self.has_commit_global(sha):
            raise RuntimeError("Cherry-pick SHA %s error beacuse SHA not exists, please make sure you local default branch is up-to-date", sha)
        if self.has_commit_current(sha):
            print("SHA %s already in current active branch, skip it.", sha)
        self.repo.git.cherry_pick(sha)
        # popen = Popen(f"git cherry-pick -x {sha}", shell=True, stdin=PIPE, stdout=PIPE, stderr=STDOUT, close_fds=True)
        # stdout, nothing = popen.communicate()
        # if stdout:
        #     raise RuntimeError("Cherry-pick SHA: %s error with message %s, please make sure you local default branch is up-to-date", (sha, stdout))
