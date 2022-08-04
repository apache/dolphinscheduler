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

"""Test resource definition."""
import pytest

from pydolphinscheduler.core.resource import Resource
from pydolphinscheduler.exceptions import PyDSParamException


def test_resource():
    """Test resource set attributes which get with same type."""
    name = "/dev/test.py"
    content = """print("hello world")"""
    description = "hello world"
    user_name = "test_user"
    expect = {
        "name": name,
        "content": content,
        "description": description,
        "userName": user_name,
    }
    resourceDefinition = Resource(
        name=name, content=content, description=description, user_name=user_name
    )
    assert resourceDefinition.get_define() == expect


def test_empty_user_name():
    """Tests for the exception get info from database when the user name is null."""
    name = "/dev/test.py"
    content = """print("hello world")"""
    description = "hello world"
    resourceDefinition = Resource(name=name, content=content, description=description)
    with pytest.raises(
        PyDSParamException,
        match="`user_name` is required when querying resources from python gate.",
    ):
        resourceDefinition.get_info_from_database()


def test_empty_content():
    """Tests for the exception create or update resource when the user name or content is empty."""
    name = "/dev/test.py"
    user_name = "test_user"
    description = "hello world"
    resourceDefinition = Resource(
        name=name, description=description, user_name=user_name
    )
    with pytest.raises(
        PyDSParamException,
        match="`user_name` and `content` are required when create or update resource from python gate.",
    ):
        resourceDefinition.create_or_update_resource()
