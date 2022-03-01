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

"""Test default config file."""

from typing import Dict

import yaml

from tests.testing.path import path_default_config_yaml


def nested_key_check(test_dict: Dict) -> None:
    """Test whether default configuration file exists specific character."""
    for key, val in test_dict.items():
        assert "." not in key, f"There is not allowed special character in key `{key}`."
        if isinstance(val, dict):
            nested_key_check(val)


def test_key_without_dot_delimiter():
    """Test wrapper of whether default configuration file exists specific character."""
    with open(path_default_config_yaml, "r") as f:
        default_config = yaml.safe_load(f)
        nested_key_check(default_config)
