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

"""Test command line interface subcommand `version`."""

import pytest

from pydolphinscheduler import __version__
from pydolphinscheduler.cli.commands import cli
from tests.testing.cli import CliTestWrapper


def test_version():
    """Test whether subcommand `version` correct."""
    cli_test = CliTestWrapper(cli, ["version"])
    cli_test.assert_success(output=f"{__version__}")


@pytest.mark.parametrize(
    "part, idx",
    [
        ("major", 0),
        ("minor", 1),
        ("micro", 2),
    ],
)
def test_version_part(part: str, idx: int):
    """Test subcommand `version` option `--part`."""
    cli_test = CliTestWrapper(cli, ["version", "--part", part])
    cli_test.assert_success(output=f"{__version__.split('.')[idx]}")


@pytest.mark.parametrize(
    "option, output",
    [
        # not support option
        (["version", "--not-support"], "No such option"),
        # not support option value
        (["version", "--part", "abc"], "Invalid value for '--part'"),
    ],
)
def test_version_not_support_option(option, output):
    """Test subcommand `version` not support option or option value."""
    cli_test = CliTestWrapper(cli, option)
    cli_test.assert_fail(ret_code=2, output=output, fuzzy=True)
