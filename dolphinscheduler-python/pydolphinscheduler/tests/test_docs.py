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

"""Test pydolphinscheduler docs."""

import re

from tests.testing.constants import task_without_example
from tests.testing.path import get_doc_tasks, get_tasks

ignore_code_file = {"__init__.py"}
ignore_doc_file = {"index.rst"}


def test_without_missing_task_rst():
    """Test without missing any task document by compare filename.

    Avoiding add new type of tasks but without adding document about it.
    """
    code_files = {p.stem for p in get_tasks(ignore_name=ignore_code_file)}
    doc_files = {p.stem for p in get_doc_tasks(ignore_name=ignore_doc_file)}
    assert code_files == doc_files


def test_task_without_example():
    """Test task document which without example.

    Avoiding add new type of tasks but without adding example content describe how to use it.
    """
    task_without_example_detected = set()
    pattern = re.compile("Example\n-------")

    for doc in get_doc_tasks(ignore_name=ignore_doc_file):
        search_result = pattern.search(doc.read_text())
        if not search_result:
            task_without_example_detected.add(doc.stem)
    assert task_without_example == task_without_example_detected


def test_doc_automodule_directive_name():
    """Test task document with correct name in directive automodule."""
    pattern = re.compile(".. automodule:: (.*)")
    for doc in get_doc_tasks(ignore_name=ignore_doc_file):
        match_string = pattern.search(doc.read_text()).group(1)
        assert f"pydolphinscheduler.tasks.{doc.stem}" == match_string
