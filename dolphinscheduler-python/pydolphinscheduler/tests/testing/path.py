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

"""Handle path related issue in test module."""

from pathlib import Path
from typing import Any, Generator

project_root = Path(__file__).parent.parent.parent

path_code_tasks = project_root.joinpath("src", "pydolphinscheduler", "tasks")
path_example = project_root.joinpath("src", "pydolphinscheduler", "examples")
path_yaml_example = project_root.joinpath("examples", "yaml_define")
path_doc_tasks = project_root.joinpath("docs", "source", "tasks")
path_default_config_yaml = project_root.joinpath(
    "src", "pydolphinscheduler", "default_config.yaml"
)


def get_all_examples() -> Generator[Path, Any, None]:
    """Get all examples files path in examples directory."""
    return (ex for ex in path_example.iterdir() if ex.is_file())


def get_tasks(ignore_name: set = None) -> Generator[Path, Any, None]:
    """Get all tasks files path in src/pydolphinscheduler/tasks directory."""
    if not ignore_name:
        ignore_name = set()
    return (
        ex
        for ex in path_code_tasks.iterdir()
        if ex.is_file() and ex.name not in ignore_name
    )


def get_doc_tasks(ignore_name: set = None) -> Generator[Path, Any, None]:
    """Get all tasks document path in docs/source/tasks directory."""
    if not ignore_name:
        ignore_name = set()
    return (
        ex
        for ex in path_doc_tasks.iterdir()
        if ex.is_file() and ex.name not in ignore_name
    )
