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

"""Test whether success submit examples DAG to PythonGatewayServer."""

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
    """Test execute examples and submit DAG to PythonGatewayServer."""
    try:
        exec(example_path.read_text())
    except Exception:
        raise Exception("Run example %s failed.", example_path.stem)
