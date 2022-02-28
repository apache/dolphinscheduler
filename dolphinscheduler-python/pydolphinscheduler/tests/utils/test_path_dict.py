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

"""Test utils.path_dict module."""

import copy
from typing import Dict, Tuple

import pytest

from pydolphinscheduler.utils.path_dict import PathDict

src_dict_list = [
    # dict with one single level
    {"a": 1},
    # dict with two levels, with same nested keys 'b'
    {"a": 1, "b": 2, "c": {"d": 3}, "e": {"b": 4}},
    # dict with three levels, with same nested keys 'b'
    {"a": 1, "b": 2, "c": {"d": 3}, "e": {"b": {"b": 4}, "f": 5}},
    # dict with specific key container
    {
        "a": 1,
        "a-b": 2,
    },
]


@pytest.mark.parametrize("org", src_dict_list)
def test_val_between_dict_and_path_dict(org: Dict):
    """Test path dict equal to original dict."""
    path_dict = PathDict(org)
    assert org == dict(path_dict)


def test_path_dict_basic_attr_access():
    """Test basic behavior of path dict.

    Including add by attribute, with simple, nested dict, and specific key dict.
    """
    expect = copy.deepcopy(src_dict_list[2])
    path_dict = PathDict(expect)

    # Add node with one level
    val = 3
    path_dict.f = val
    expect.update({"f": val})
    assert expect == path_dict

    # Add node with multiple level
    val = {"abc": 123}
    path_dict.e.g = val
    expect.update({"e": {"b": {"b": 4}, "f": 5, "g": val}})
    assert expect == path_dict

    # Specific key
    expect = copy.deepcopy(src_dict_list[3])
    path_dict = PathDict(expect)
    assert 1 == path_dict.a
    assert 2 == getattr(path_dict, "a-b")


@pytest.mark.parametrize(
    "org, exists, not_exists",
    [
        (
            src_dict_list[0],
            ("a"),
            ("b", "a.b"),
        ),
        (
            src_dict_list[1],
            ("a", "b", "c", "e", "c.d", "e.b"),
            ("a.b", "c.e", "b.c", "b.e"),
        ),
        (
            src_dict_list[2],
            ("a", "b", "c", "e", "c.d", "e.b", "e.b.b", "e.b.b", "e.f"),
            ("a.b", "c.e", "b.c", "b.e", "b.b.f", "b.f"),
        ),
    ],
)
def test_path_dict_attr(org: Dict, exists: Tuple, not_exists: Tuple):
    """Test properties' integrity of path dict."""
    path_dict = PathDict(org)
    assert all([hasattr(path_dict, path) for path in exists])
    # assert not any([hasattr(path_dict, path) for path in not_exists])


@pytest.mark.parametrize(
    "org, path_get",
    [
        (
            src_dict_list[0],
            {"a": 1},
        ),
        (
            src_dict_list[1],
            {
                "a": 1,
                "b": 2,
                "c": {"d": 3},
                "c.d": 3,
                "e": {"b": 4},
                "e.b": 4,
            },
        ),
        (
            src_dict_list[2],
            {
                "a": 1,
                "b": 2,
                "c": {"d": 3},
                "c.d": 3,
                "e": {"b": {"b": 4}, "f": 5},
                "e.b": {"b": 4},
                "e.b.b": 4,
                "e.f": 5,
            },
        ),
    ],
)
def test_path_dict_get(org: Dict, path_get: Dict):
    """Test path dict getter function."""
    path_dict = PathDict(org)
    assert all([path_get[path] == path_dict.__getattr__(path) for path in path_get])


@pytest.mark.parametrize(
    "org, path_set, expect",
    [
        # Add not exists node
        (
            src_dict_list[0],
            {"b": 2},
            {
                "a": 1,
                "b": 2,
            },
        ),
        # Overwrite exists node with different type of value
        (
            src_dict_list[0],
            {"a": "b"},
            {"a": "b"},
        ),
        # Add multiple not exists node with variable types of value
        (
            src_dict_list[0],
            {
                "b.c.d": 123,
                "b.c.e": "a",
                "b.f": {"g": 23, "h": "bc", "i": {"j": "k"}},
            },
            {
                "a": 1,
                "b": {
                    "c": {
                        "d": 123,
                        "e": "a",
                    },
                    "f": {"g": 23, "h": "bc", "i": {"j": "k"}},
                },
            },
        ),
        # Test complex original data
        (
            src_dict_list[2],
            {
                "g": 12,
                "c.h": 34,
            },
            {
                "a": 1,
                "b": 2,
                "g": 12,
                "c": {"d": 3, "h": 34},
                "e": {"b": {"b": 4}, "f": 5},
            },
        ),
    ],
)
def test_path_dict_set(org: Dict, path_set: Dict, expect: Dict):
    """Test path dict setter function."""
    path_dict = PathDict(org)
    for path in path_set:
        path_dict.__setattr__(path, path_set[path])
    assert expect == path_dict
