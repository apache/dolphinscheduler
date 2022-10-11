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

"""Main function for releasing."""

import argparse
import os

from github.changelog import Changelog
from github.git import Git
from github.pull_request import PullRequest
from github.user import User


def get_changelog(access_token: str, milestone: str) -> str:
    """Get changelog in specific milestone from GitHub Restful API."""
    pr = PullRequest(token=access_token)
    pr_merged = pr.search_merged_by_milestone(milestone)
    # Sort according to merged time ascending
    pr_merged_sort = sorted(pr_merged, key=lambda p: p["closed_at"])

    changelog = Changelog(pr_merged_sort)
    changelog_text = changelog.generate()
    return changelog_text


def get_contributor(access_token: str, milestone: str) -> str:
    """Get contributor in specific milestone from GitHub Restful API."""
    pr = PullRequest(token=access_token)
    pr_merged = pr.search_merged_by_milestone(milestone)

    users = User(prs=pr_merged)
    contributor = users.contributors()
    # Sort according alphabetical
    return ", ".join(sorted(contributor))


def auto_cherry_pick(access_token: str, milestone: str) -> None:
    """Do git cherry-pick in specific milestone, require update dev branch."""
    pr = PullRequest(token=access_token)
    pr_merged = pr.search_merged_by_milestone(milestone)
    # Sort according to merged time ascending
    pr_merged_sort = sorted(pr_merged, key=lambda p: p["closed_at"])

    for p in pr_merged_sort:
        pr_detail = pr.get_merged_detail(p["number"])
        print(f"git cherry-pick -x {pr_detail['merge_commit_sha']}")
        Git().cherry_pick_pr(pr_detail)


def build_argparse() -> argparse.ArgumentParser:
    """Build argparse.ArgumentParser with specific configuration."""
    parser = argparse.ArgumentParser(prog="release")

    subparsers = parser.add_subparsers(
        title="subcommands",
        dest="subcommand",
        help="Choose one of the subcommand you want to run.",
    )
    parser_check = subparsers.add_parser(
        "changelog", help="Generate changelog from specific milestone."
    )
    parser_check.set_defaults(func=get_changelog)

    parser_prune = subparsers.add_parser(
        "contributor", help="List all contributors from specific milestone."
    )
    parser_prune.set_defaults(func=get_contributor)

    parser_prune = subparsers.add_parser(
        "cherry-pick",
        help="Auto cherry pick pr to current branch from specific milestone.",
    )
    parser_prune.set_defaults(func=auto_cherry_pick)

    return parser


if __name__ == "__main__":
    arg_parser = build_argparse()
    # args = arg_parser.parse_args(["cherry-pick"])
    args = arg_parser.parse_args()

    ENV_ACCESS_TOKEN = os.environ.get("GH_ACCESS_TOKEN", None)
    ENV_MILESTONE = os.environ.get("GH_REPO_MILESTONE", None)

    if ENV_ACCESS_TOKEN is None or ENV_MILESTONE is None:
        raise RuntimeError(
            "Environment variable `GH_ACCESS_TOKEN` and `GH_REPO_MILESTONE` must provider"
        )

    print(args.func(ENV_ACCESS_TOKEN, ENV_MILESTONE))
