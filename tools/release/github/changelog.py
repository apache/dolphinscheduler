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

"""Github utils for release changelog."""

from typing import Dict, List


class Changelog:
    """Generate changelog according specific pull requests list.

    Each pull requests will only once in final result. If pull requests have more than one label we need,
    will classify to high priority label type, currently priority is
    `feature > bug > improvement > document > chore`. pr will into feature section if it with both `feature`,
    `improvement`, `document` label.

    :param prs: pull requests list.
    """

    key_number = "number"
    key_labels = "labels"
    key_name = "name"

    label_feature = "feature"
    label_bug = "bug"
    label_improvement = "improvement"
    label_document = "document"
    label_chore = "chore"

    changelog_prefix = "\n\n<details><summary>Click to expand</summary>\n\n"
    changelog_suffix = "\n\n</details>\n"

    def __init__(self, prs: List[Dict]):
        self.prs = prs
        self.features = []
        self.bugfixs = []
        self.improvements = []
        self.documents = []
        self.chores = []

    def generate(self) -> str:
        """Generate changelog."""
        self.classify()
        final = []
        if self.features:
            detail = f"## Feature{self.changelog_prefix}{self._convert(self.features)}{self.changelog_suffix}"
            final.append(detail)
        if self.improvements:
            detail = (
                f"## Improvement{self.changelog_prefix}"
                f"{self._convert(self.improvements)}{self.changelog_suffix}"
            )
            final.append(detail)
        if self.bugfixs:
            detail = f"## Bugfix{self.changelog_prefix}{self._convert(self.bugfixs)}{self.changelog_suffix}"
            final.append(detail)
        if self.documents:
            detail = (
                f"## Document{self.changelog_prefix}"
                f"{self._convert(self.documents)}{self.changelog_suffix}"
            )
            final.append(detail)
        if self.chores:
            detail = f"## Chore{self.changelog_prefix}{self._convert(self.chores)}{self.changelog_suffix}"
            final.append(detail)
        return "\n".join(final)

    @staticmethod
    def _convert(prs: List[Dict]) -> str:
        """Convert pull requests into changelog item text."""
        return "\n".join(
            [f"- {pr['title']} (#{pr['number']}) @{pr['user']['login']}" for pr in prs]
        )

    def classify(self) -> None:
        """Classify pull requests different kinds of section in changelog.

        Each pull requests only belongs to one single classification.
        """
        for pr in self.prs:
            if self.key_labels not in pr:
                raise KeyError("PR %s do not have labels", pr[self.key_number])
            if self._is_feature(pr):
                self.features.append(pr)
            elif self._is_bugfix(pr):
                self.bugfixs.append(pr)
            elif self._is_improvement(pr):
                self.improvements.append(pr)
            elif self._is_document(pr):
                self.documents.append(pr)
            elif self._is_chore(pr):
                self.chores.append(pr)
            else:
                raise KeyError(
                    "There must at least one of labels `feature|bug|improvement|document|chore`"
                    "but it do not, pr: %s",
                    pr["html_url"],
                )

    def _is_feature(self, pr: Dict) -> bool:
        """Belong to feature pull requests."""
        return any(
            [
                label[self.key_name] == self.label_feature
                for label in pr[self.key_labels]
            ]
        )

    def _is_bugfix(self, pr: Dict) -> bool:
        """Belong to bugfix pull requests."""
        return any(
            [label[self.key_name] == self.label_bug for label in pr[self.key_labels]]
        )

    def _is_improvement(self, pr: Dict) -> bool:
        """Belong to improvement pull requests."""
        return any(
            [
                label[self.key_name] == self.label_improvement
                for label in pr[self.key_labels]
            ]
        )

    def _is_document(self, pr: Dict) -> bool:
        """Belong to document pull requests."""
        return any(
            [
                label[self.key_name] == self.label_document
                for label in pr[self.key_labels]
            ]
        )

    def _is_chore(self, pr: Dict) -> bool:
        """Belong to chore pull requests."""
        return any(
            [label[self.key_name] == self.label_chore for label in pr[self.key_labels]]
        )
