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

from typing import Dict, List, Optional

from github.resp_get import RespGet


class PullRequest:
    """Pull request to filter the by specific condition.

    :param token: token to request GitHub API entrypoint.
    :param repo: GitHub repository identify, use `user/repo` or `org/repo`.
    """

    url_search = "https://api.github.com/search/issues"
    url_pr = "https://api.github.com/repos/{}/pulls/{}"

    def __init__(self, token: str, repo: Optional[str] = "apache/dolphinscheduler"):
        self.token = token
        self.repo = repo
        self.headers = {
            "Accept": "application/vnd.github+json",
            "Authorization": f"token {token}",
        }

    def get_merged_detail(self, number: str) -> Dict:
        """Get all merged pull requests detail by pr number.

        :param number: pull requests number you want to get detail.
        """
        return RespGet(
            url=self.url_pr.format(self.repo, number), headers=self.headers
        ).get_single()

    def get_merged_detail_by_milestone(self, milestone: str) -> List[Dict]:
        """Get all merged requests pull request detail by specific milestone.

        :param milestone: query by specific milestone.
        """
        detail = []
        numbers = {
            pr.get("number") for pr in self.search_merged_by_milestone(milestone)
        }
        for number in numbers:
            pr_dict = RespGet(
                url=self.url_pr.format(self.repo, number), headers=self.headers
            ).get_single()
            detail.append(pr_dict)
        return detail

    def search_merged_by_milestone(self, milestone: str) -> List[Dict]:
        """Get all merged requests pull request by specific milestone.

        :param milestone: query by specific milestone.
        """
        params = {"q": f"repo:{self.repo} is:pr is:merged milestone:{milestone}"}
        return RespGet(
            url=self.url_search, headers=self.headers, param=params
        ).get_total()
