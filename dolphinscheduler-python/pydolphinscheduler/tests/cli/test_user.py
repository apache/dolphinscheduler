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

from pydolphinscheduler.cli.commands import cli
from tests.testing.cli import CliTestWrapper


@pytest.mark.parametrize(
    "option, output",
    [
        (["user", "--setter", "test-name", "test-password", "test-email@abc.com", "17366637777", "test-tenant",
          "test-queue", 1], "Set user start"),
    ],
)
def test_user_setter(option, output):
    """Test subcommand `user` option `--setter`."""
    cli_test = CliTestWrapper(cli, option)
    print(cli_test.result.output)
    print(cli_test.show_result_output())
    cli_test.assert_success(output=output)
