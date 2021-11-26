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

"""Test utils.string module."""

import pytest

from pydolphinscheduler.utils.string import attr2camel, class_name2camel, snake2camel


@pytest.mark.parametrize(
    "snake, expect",
    [
        ("snake_case", "snakeCase"),
        ("snake_123case", "snake123Case"),
        ("snake_c_a_s_e", "snakeCASE"),
        ("snake__case", "snakeCase"),
        ("snake_case_case", "snakeCaseCase"),
        ("_snake_case", "SnakeCase"),
        ("__snake_case", "SnakeCase"),
        ("Snake_case", "SnakeCase"),
    ],
)
def test_snake2camel(snake: str, expect: str):
    """Test function snake2camel, this is a base function for utils.string."""
    assert expect == snake2camel(
        snake
    ), f"Test case {snake} do no return expect result {expect}."


@pytest.mark.parametrize(
    "attr, expects",
    [
        # source attribute, (true expect, false expect),
        ("snake_case", ("snakeCase", "snakeCase")),
        ("snake_123case", ("snake123Case", "snake123Case")),
        ("snake_c_a_s_e", ("snakeCASE", "snakeCASE")),
        ("snake__case", ("snakeCase", "snakeCase")),
        ("snake_case_case", ("snakeCaseCase", "snakeCaseCase")),
        ("_snake_case", ("snakeCase", "SnakeCase")),
        ("__snake_case", ("snakeCase", "SnakeCase")),
        ("Snake_case", ("SnakeCase", "SnakeCase")),
    ],
)
def test_attr2camel(attr: str, expects: tuple):
    """Test function attr2camel."""
    for idx, expect in enumerate(expects):
        include_private = idx % 2 == 0
        assert expect == attr2camel(
            attr, include_private
        ), f"Test case {attr} do no return expect result {expect} when include_private is {include_private}."


@pytest.mark.parametrize(
    "class_name, expect",
    [
        ("snake_case", "snakeCase"),
        ("snake_123case", "snake123Case"),
        ("snake_c_a_s_e", "snakeCASE"),
        ("snake__case", "snakeCase"),
        ("snake_case_case", "snakeCaseCase"),
        ("_snake_case", "snakeCase"),
        ("_Snake_case", "snakeCase"),
        ("__snake_case", "snakeCase"),
        ("__Snake_case", "snakeCase"),
        ("Snake_case", "snakeCase"),
    ],
)
def test_class_name2camel(class_name: str, expect: str):
    """Test function class_name2camel."""
    assert expect == class_name2camel(
        class_name
    ), f"Test case {class_name} do no return expect result {expect}."
