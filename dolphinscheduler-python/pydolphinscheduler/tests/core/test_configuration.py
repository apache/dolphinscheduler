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

"""Test class :mod:`pydolphinscheduler.core.configuration`' method."""

import os
from pathlib import Path

import pytest

from pydolphinscheduler.core import configuration


@pytest.mark.parametrize(
    "env, expect",
    [
        (None, "~/pydolphinscheduler"),
        ("/tmp/pydolphinscheduler", "/tmp/pydolphinscheduler"),
        ("/tmp/test_abc", "/tmp/test_abc"),
    ],
)
def test_get_config_file_path(env, expect):
    """Test get config file path method."""
    # Avoid env setting by other tests
    os.environ.pop("PYDOLPHINSCHEDULER_HOME", None)
    if env:
        os.environ["PYDOLPHINSCHEDULER_HOME"] = env
    assert (
        Path(expect).joinpath("config.yaml").expanduser()
        == configuration.get_config_file_path()
    )
