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

"""Test file utils."""

import shutil
from pathlib import Path

import pytest

from pydolphinscheduler.utils import file
from tests.testing.file import delete_file, get_file_content

content = "test_content"
file_path = "/tmp/test/file/test_file_write.txt"


@pytest.fixture
def teardown_del_file():
    """Teardown about delete file."""
    yield
    delete_file(file_path)


@pytest.fixture
def setup_crt_first():
    """Set up and teardown about create file first and then delete it."""
    file.write(content=content, to_path=file_path)
    yield
    delete_file(file_path)


def test_write_content(teardown_del_file):
    """Test function :func:`write` on write behavior with correct content."""
    assert not Path(file_path).exists()
    file.write(content=content, to_path=file_path)
    assert Path(file_path).exists()
    assert content == get_file_content(file_path)


def test_write_not_create_parent(teardown_del_file):
    """Test function :func:`write` with parent not exists and do not create path."""
    file_test_dir = Path(file_path).parent
    if file_test_dir.exists():
        shutil.rmtree(str(file_test_dir))
    assert not file_test_dir.exists()
    with pytest.raises(
        ValueError,
        match="Parent directory do not exists and set param `create` to `False`",
    ):
        file.write(content=content, to_path=file_path, create=False)


def test_write_overwrite(setup_crt_first):
    """Test success with file exists but set ``True`` to overwrite."""
    assert Path(file_path).exists()

    new_content = f"new_{content}"
    file.write(content=new_content, to_path=file_path, overwrite=True)
    assert new_content == get_file_content(file_path)


def test_write_overwrite_error(setup_crt_first):
    """Test error with file exists but set ``False`` to overwrite."""
    assert Path(file_path).exists()

    new_content = f"new_{content}"
    with pytest.raises(
        FileExistsError, match=".*already exists and you choose not overwrite mode\\."
    ):
        file.write(content=new_content, to_path=file_path)
