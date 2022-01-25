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
from pathlib import Path

path_code_base = Path(__file__).parent.parent.joinpath(
    "src", "pydolphinscheduler", "tasks"
)
path_doc_base = Path(__file__).parent.parent.joinpath("docs", "source", "tasks")

ignore_code_file = {"__init__.py"}

ignore_doc_file = {"index.rst"}


def test_without_missing_task_rst():
    """Test without missing any task document by compare filename."""
    code_files = {
        file.stem
        for file in path_code_base.iterdir()
        if file.is_file() and file.name not in ignore_code_file
    }
    doc_files = {
        file.stem
        for file in path_doc_base.iterdir()
        if file.is_file() and file.name not in ignore_doc_file
    }
    assert code_files == doc_files


def test_doc_automodule_directive_name():
    """Test task document with correct name in directive automodule."""
    pattern = re.compile(".. automodule:: (.*)")
    for doc in path_doc_base.iterdir():
        if doc.is_file() and doc.name not in ignore_doc_file:
            match_string = pattern.search(doc.read_text()).group(1)
            assert f"pydolphinscheduler.tasks.{doc.stem}" == match_string
