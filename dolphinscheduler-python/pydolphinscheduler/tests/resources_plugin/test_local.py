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

"""Test local resource plugin."""
from pathlib import Path
from unittest.mock import PropertyMock, patch

import pytest

from pydolphinscheduler.core import Task
from pydolphinscheduler.exceptions import PyResPluginException
from pydolphinscheduler.resources_plugin.local import Local
from pydolphinscheduler.utils import file
from tests.testing.file import delete_file

file_name = "local_res.sh"
file_content = "echo Test local res plugin"
res_plugin_prefix = Path(__file__).parent
file_path = res_plugin_prefix.joinpath(file_name)


@pytest.fixture()
def setup_crt_first():
    """Set up and teardown about create file first and then delete it."""
    file.write(content=file_content, to_path=file_path)
    yield
    delete_file(file_path)


@pytest.mark.parametrize(
    "val, expected",
    [
        (file_name, file_content),
    ],
)
@patch(
    "pydolphinscheduler.core.task.Task.gen_code_and_version",
    return_value=(123, 1),
)
@patch(
    "pydolphinscheduler.core.task.Task.ext",
    new_callable=PropertyMock,
    return_value={
        ".sh",
    },
)
@patch(
    "pydolphinscheduler.core.task.Task.ext_attr",
    new_callable=PropertyMock,
    return_value="_raw_script",
)
@patch(
    "pydolphinscheduler.core.task.Task._raw_script",
    create=True,
    new_callable=PropertyMock,
)
def test_task_obtain_res_plugin(
    m_raw_script, m_ext_attr, m_ext, m_code_version, val, expected, setup_crt_first
):
    """Test task obtaining resource plug-in."""
    m_raw_script.return_value = val
    task = Task(
        name="test_task_ext_attr",
        task_type="type",
        resource_plugin=Local(str(res_plugin_prefix)),
    )
    assert expected == getattr(task, "raw_script")


@pytest.mark.parametrize(
    "attr, expected",
    [({"prefix": res_plugin_prefix, "file_name": file_name}, file_content)],
)
def test_local_res_read_file(attr, expected, setup_crt_first):
    """Test the read_file function of the local resource plug-in."""
    local = Local(str(attr.get("prefix")))
    local.read_file(attr.get("file_name"))
    assert expected == local.read_file(file_name)


@pytest.mark.parametrize(
    "attr",
    [
        {"prefix": res_plugin_prefix, "file_name": file_name},
    ],
)
def test_local_res_file_not_found(attr):
    """Test local resource plugin file does not exist."""
    with pytest.raises(
        PyResPluginException,
        match=".* is not found",
    ):
        local = Local(str(attr.get("prefix")))
        local.read_file(attr.get("file_name"))
