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

"""Utils of command line test."""


from click.testing import CliRunner

from tests.testing.constants import DEV_MODE


class CliTestWrapper:
    """Wrap command click CliRunner.invoke."""

    def __init__(self, *args, **kwargs):
        runner = CliRunner()
        self.result = runner.invoke(*args, **kwargs)
        self.show_result_output()

    def _assert_output(self, output: str = None, fuzzy: bool = False):
        """Assert between `CliRunner.invoke.result.output` and parameter `output`.

        :param output: The output will check compare to the ``CliRunner.invoke.output``.
        :param fuzzy: A flag define whether assert :param:`output` in fuzzy or not.
            Check if `CliRunner.invoke.output` contain :param:`output` is set ``True``
            and CliRunner.invoke.output equal to :param:`output` if we set it ``False``.
        """
        if not output:
            return
        if fuzzy:
            assert output in self.result.output
        else:
            assert self.result.output.rstrip("\n") == output

    def show_result_output(self):
        """Print `CliRunner.invoke.result` output content in debug mode.

        It read variable named `PY_DOLPHINSCHEDULER_DEV_MODE` from env, when it set to `true` or `t` or `1`
        will print result output when class :class:`CliTestWrapper` is initialization.
        """
        if DEV_MODE:
            print(f"\n{self.result.output}\n")

    def assert_success(self, output: str = None, fuzzy: bool = False):
        """Assert test is success.

        It would check whether `CliRunner.invoke.exit_code` equals to `0`, with no
        exception at the same time. It's also can test the content of `CliRunner.invoke.output`.

        :param output: The output will check compare to the ``CliRunner.invoke.output``.
        :param fuzzy: A flag define whether assert :param:`output` in fuzzy or not.
            Check if `CliRunner.invoke.output` contain :param:`output` is set ``True``
            and CliRunner.invoke.output equal to :param:`output` if we set it ``False``.
        """
        assert self.result.exit_code == 0
        if self.result.exception:
            raise self.result.exception
        self._assert_output(output, fuzzy)

    def assert_fail(self, ret_code: int, output: str = None, fuzzy: bool = False):
        """Assert test is fail.

        It would check whether `CliRunner.invoke.exit_code` equals to :param:`ret_code`,
        and it will also can test the content of `CliRunner.invoke.output`.

        :param ret_code: The returning code of this fail test.
        :param output: The output will check compare to the ``CliRunner.invoke.output``.
        :param fuzzy: A flag define whether assert :param:`output` in fuzzy or not.
            Check if `CliRunner.invoke.output` contain :param:`output` is set ``True``
            and CliRunner.invoke.output equal to :param:`output` if we set it ``False``.
        """
        assert ret_code == self.result.exit_code
        self._assert_output(output, fuzzy)
