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

"""Test whether success submit examples DAG to PythonGatewayService."""

import subprocess
from pathlib import Path

import pytest

from tests.testing.constants import ignore_exec_examples
from tests.testing.path import path_example


@pytest.mark.parametrize(
    "example_path",
    [
        path
        for path in path_example.iterdir()
        if path.is_file() and path.stem not in ignore_exec_examples
    ],
)
def test_exec_white_list_example(example_path: Path):
    """Test execute examples and submit DAG to PythonGatewayService."""
    try:
        # Because our task decorator used module ``inspect`` to get the source, and it will
        # raise IOError when call it by built-in function ``exec``, so we change to ``subprocess.check_call``
        subprocess.check_call(["python", str(example_path)])
    except subprocess.CalledProcessError:
        raise RuntimeError("Run example %s failed.", example_path.stem)


def test_exec_multiple_times():
    """Test whether process definition can be executed more than one times."""
    tutorial_path = path_example.joinpath("tutorial.py")
    time = 0
    while time < 3:
        try:
            subprocess.check_call(["python", str(tutorial_path)])
        except subprocess.CalledProcessError:
            raise RuntimeError("Run example %s failed.", tutorial_path.stem)
        time += 1
